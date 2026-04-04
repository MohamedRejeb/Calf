use jni::objects::{JClass, JObjectArray, JString};
use jni::sys::{jint, jlong};
use jni::JNIEnv;

// Result codes returned to Kotlin
const RESULT_SUCCESS: jint = 0;
const RESULT_DISMISSED: jint = 1;
const RESULT_UNAVAILABLE: jint = 2;

/// No-op — forces the library to load eagerly from Kotlin.
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_share_platform_NativeShareBridge_init(
    _env: JNIEnv,
    _class: JClass,
) {
}

/// Returns true if native share is supported on this platform (macOS only).
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_share_platform_NativeShareBridge_isNativeShareSupported(
    _env: JNIEnv,
    _class: JClass,
) -> bool {
    cfg!(target_os = "macos")
}

/// Show the native share sheet.
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_share_platform_NativeShareBridge_showShareSheet(
    mut env: JNIEnv,
    _class: JClass,
    text: JString,
    url: JString,
    file_paths: JObjectArray,
    parent_window: jlong,
) -> jint {
    let text_str = jstring_to_string(&mut env, &text);
    let url_str = jstring_to_string(&mut env, &url);
    let paths = if file_paths.is_null() {
        Vec::new()
    } else {
        jstring_array_to_vec(&mut env, &file_paths)
    };

    show_share_sheet(text_str, url_str, paths, parent_window)
}

fn jstring_to_string(env: &mut JNIEnv, js: &JString) -> Option<String> {
    if js.is_null() {
        return None;
    }
    env.get_string(js).ok().map(|s| s.into())
}

fn jstring_array_to_vec(env: &mut JNIEnv, array: &JObjectArray) -> Vec<String> {
    let len = env.get_array_length(array).unwrap_or(0);
    let mut result = Vec::with_capacity(len as usize);
    for i in 0..len {
        if let Ok(obj) = env.get_object_array_element(array, i) {
            let jstr: JString = obj.into();
            if let Some(s) = jstring_to_string(env, &jstr) {
                result.push(s);
            }
            let _ = env.delete_local_ref(jstr);
        }
    }
    result
}

// ---- macOS implementation ----

#[cfg(target_os = "macos")]
fn show_share_sheet(
    text: Option<String>,
    url: Option<String>,
    file_paths: Vec<String>,
    parent_window: jlong,
) -> jint {
    use std::ffi::c_void;
    use std::sync::{Arc, Condvar, Mutex};

    use objc2::rc::Retained;
    use objc2::runtime::{AnyClass, AnyObject, Sel};
    #[allow(deprecated)]
    use objc2::msg_send_id;
    use objc2::{class, msg_send, sel, MainThreadMarker};
    use objc2::declare::ClassBuilder;
    use objc2_app_kit::{NSApplication, NSEvent, NSView, NSWindow};
    use objc2_foundation::{
        NSArray, NSObject, NSPoint, NSRect, NSSize, NSString, NSURL,
    };

    // Build the items array
    let mut items: Vec<Retained<NSObject>> = Vec::new();

    if let Some(ref t) = text {
        let ns_string = NSString::from_str(t);
        items.push(Retained::into_super(ns_string));
    }

    if let Some(ref u) = url {
        let ns_url_string = NSString::from_str(u);
        if let Some(ns_url) = NSURL::URLWithString(&ns_url_string) {
            items.push(Retained::into_super(ns_url));
        }
    }

    for path in &file_paths {
        let ns_path = NSString::from_str(path);
        let file_url = NSURL::fileURLWithPath(&ns_path);
        items.push(Retained::into_super(file_url));
    }

    if items.is_empty() {
        return RESULT_UNAVAILABLE;
    }

    // Shared state for completion signaling
    let pair = Arc::new((Mutex::new(None::<jint>), Condvar::new()));
    let pair_for_block = pair.clone();

    let items_array = NSArray::from_retained_slice(&items);

    // Register a delegate class at runtime using ClassBuilder.
    // The delegate stores a pointer to the Arc pair in an ivar.
    static DELEGATE_CLASS_INIT: std::sync::Once = std::sync::Once::new();
    static mut DELEGATE_CLASS: *const AnyClass = std::ptr::null();

    DELEGATE_CLASS_INIT.call_once(|| {
        let superclass = AnyClass::get(c"NSObject").unwrap();
        let mut builder = ClassBuilder::new(c"CalfShareDelegate", superclass).unwrap();

        // Add an ivar to store the context pointer
        builder.add_ivar::<*mut c_void>(c"_pairPtr");

        // sharingServicePicker:didChooseSharingService:
        unsafe extern "C" fn did_choose(
            this: *const AnyObject,
            _sel: Sel,
            _picker: *const AnyObject,
            service: *const AnyObject,
        ) {
            unsafe {
                let ivar = AnyClass::get(c"CalfShareDelegate")
                    .unwrap()
                    .instance_variable(c"_pairPtr")
                    .unwrap();
                let ptr: *mut c_void = *ivar.load::<*mut c_void>(&*this);
                if ptr.is_null() {
                    return;
                }
                // Reconstruct the Arc, signal, then forget to avoid dropping
                let pair = Arc::from_raw(ptr as *const (Mutex<Option<jint>>, Condvar));
                let chosen = !service.is_null();
                {
                    let (lock, cvar) = &*pair;
                    let mut result = lock.lock().unwrap();
                    *result = Some(if chosen { RESULT_SUCCESS } else { RESULT_DISMISSED });
                    cvar.notify_one();
                }
                // Don't drop the Arc — the waiting thread still holds one
                std::mem::forget(pair);
            }
        }

        unsafe {
            builder.add_method(
                sel!(sharingServicePicker:didChooseSharingService:),
                did_choose
                    as unsafe extern "C" fn(*const AnyObject, Sel, *const AnyObject, *const AnyObject),
            );
        }

        let cls = builder.register();
        unsafe {
            DELEGATE_CLASS = cls as *const AnyClass;
        }
    });

    // Dispatch to main thread via libdispatch
    extern "C" {
        // &_dispatch_main_q as *const c_void is a macro; the actual symbol is _dispatch_main_q
        static _dispatch_main_q: c_void;
        fn dispatch_async_f(
            queue: *const c_void,
            context: *mut c_void,
            work: extern "C" fn(*mut c_void),
        );
    }

    struct ShareContext {
        items_array: Retained<NSArray<NSObject>>,
        parent_window: jlong,
        pair: Arc<(Mutex<Option<jint>>, Condvar)>,
    }
    unsafe impl Send for ShareContext {}

    extern "C" fn execute_on_main(context: *mut c_void) {
        let ctx = unsafe { Box::from_raw(context as *mut ShareContext) };

        // Get the window: use the provided handle, or fall back to NSApp's keyWindow
        let ns_window: Retained<NSWindow> = if ctx.parent_window != 0 {
            let ns_window_ptr = ctx.parent_window as *mut NSWindow;
            match unsafe { Retained::retain(ns_window_ptr) } {
                Some(w) => w,
                None => {
                    let (lock, cvar) = &*ctx.pair;
                    *lock.lock().unwrap() = Some(RESULT_UNAVAILABLE);
                    cvar.notify_one();
                    return;
                }
            }
        } else {
            // Safe: execute_on_main runs on the main thread via dispatch_async
            let mtm = unsafe { MainThreadMarker::new_unchecked() };
            let app = NSApplication::sharedApplication(mtm);
            match app.keyWindow() {
                Some(w) => w,
                None => {
                    let (lock, cvar) = &*ctx.pair;
                    *lock.lock().unwrap() = Some(RESULT_UNAVAILABLE);
                    cvar.notify_one();
                    return;
                }
            }
        };

        let ns_view: Retained<NSView> = match ns_window.contentView() {
            Some(v) => v,
            None => {
                let (lock, cvar) = &*ctx.pair;
                *lock.lock().unwrap() = Some(RESULT_UNAVAILABLE);
                cvar.notify_one();
                return;
            }
        };

        // Create NSSharingServicePicker
        let picker_class = class!(NSSharingServicePicker);
        #[allow(deprecated)]
        let picker: Retained<AnyObject> = unsafe {
            msg_send_id![
                msg_send_id![picker_class, alloc],
                initWithItems: &*ctx.items_array
            ]
        };

        // Create delegate and store the pair pointer in its ivar
        let delegate_class = unsafe { &*DELEGATE_CLASS };
        #[allow(deprecated)]
        let delegate: Retained<AnyObject> = unsafe {
            msg_send_id![msg_send_id![delegate_class, alloc], init]
        };

        // Store Arc pointer in the ivar (increment ref count)
        let pair_ptr = Arc::into_raw(ctx.pair.clone()) as *mut c_void;
        unsafe {
            let ivar = delegate_class.instance_variable(c"_pairPtr").unwrap();
            *ivar.load_mut::<*mut c_void>(&mut *Retained::as_ptr(&delegate).cast_mut()) = pair_ptr;
        }

        // Set delegate
        let _: () = unsafe { msg_send![&picker, setDelegate: &*delegate] };

        // Determine anchor point: use mouse position if inside the view, otherwise center
        let bounds = ns_view.bounds();
        let mouse_screen = NSEvent::mouseLocation();
        // Convert screen coordinates to window coordinates, then to view coordinates
        let mouse_window = ns_window.convertPointFromScreen(mouse_screen);
        let mouse_view = ns_view.convertPoint_fromView(mouse_window, None);

        let anchor_point = if mouse_view.x >= bounds.origin.x
            && mouse_view.x <= bounds.origin.x + bounds.size.width
            && mouse_view.y >= bounds.origin.y
            && mouse_view.y <= bounds.origin.y + bounds.size.height
        {
            mouse_view
        } else {
            NSPoint::new(bounds.size.width / 2.0, bounds.size.height / 2.0)
        };

        let anchor = NSRect::new(anchor_point, NSSize::new(1.0, 1.0));
        let edge: isize = 1; // NSMinYEdge

        let _: () = unsafe {
            msg_send![
                &picker,
                showRelativeToRect: anchor,
                ofView: &*ns_view,
                preferredEdge: edge
            ]
        };

        // Prevent delegate from being deallocated while picker is alive
        std::mem::forget(delegate);
    }

    let ctx = Box::new(ShareContext {
        items_array,
        parent_window,
        pair: pair_for_block,
    });

    unsafe {
        dispatch_async_f(
            &_dispatch_main_q as *const c_void,
            Box::into_raw(ctx) as *mut c_void,
            execute_on_main,
        );
    }

    // Wait for the delegate callback
    let (lock, cvar) = &*pair;
    let mut result = lock.lock().unwrap();
    let timeout = std::time::Duration::from_secs(300);
    while result.is_none() {
        let (new_result, timeout_result) = cvar.wait_timeout(result, timeout).unwrap();
        result = new_result;
        if timeout_result.timed_out() {
            return RESULT_DISMISSED;
        }
    }

    result.unwrap_or(RESULT_DISMISSED)
}

// ---- Non-macOS fallback ----

#[cfg(not(target_os = "macos"))]
fn show_share_sheet(
    _text: Option<String>,
    _url: Option<String>,
    _file_paths: Vec<String>,
    _parent_window: jlong,
) -> jint {
    RESULT_UNAVAILABLE
}
