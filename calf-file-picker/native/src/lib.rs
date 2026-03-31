use jni::objects::{JClass, JObjectArray, JString};
use jni::sys::{jboolean, jlong, jobjectArray, jstring, JNI_TRUE};
use jni::JNIEnv;
use rfd::FileDialog;
use std::path::PathBuf;

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/// Convert a JString to a Rust String, returning None for null.
fn jstring_to_string(env: &mut JNIEnv, js: &JString) -> Option<String> {
    if js.is_null() {
        return None;
    }
    env.get_string(js).ok().map(|s| s.into())
}

/// Convert a Java String[] to Vec<String>, properly releasing JNI local references.
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

/// Convert a Vec of paths to a Java String[], with proper error handling.
fn paths_to_jstring_array(
    env: &mut JNIEnv,
    paths: &[PathBuf],
) -> Result<jobjectArray, jni::errors::Error> {
    let string_class = env.find_class("java/lang/String")?;
    let array = env.new_object_array(paths.len() as i32, &string_class, JString::default())?;

    for (i, path) in paths.iter().enumerate() {
        let path_str = match path.to_str() {
            Some(s) => s.to_owned(),
            None => path.to_string_lossy().into_owned(),
        };
        let jstr = env.new_string(&path_str)?;
        env.set_object_array_element(&array, i as i32, &jstr)?;
        env.delete_local_ref(jstr)?;
    }

    Ok(array.into_raw())
}

/// Convert a path to a jstring, returning null on failure.
fn path_to_jstring(env: &mut JNIEnv, path: &std::path::Path) -> jstring {
    let s = match path.to_str() {
        Some(s) => s.to_owned(),
        None => path.to_string_lossy().into_owned(),
    };
    env.new_string(&s)
        .map(|s| s.into_raw())
        .unwrap_or(std::ptr::null_mut())
}

/// Throw a Java RuntimeException with the given message.
fn throw_runtime_exception(env: &mut JNIEnv, msg: &str) {
    let _ = env.throw_new("java/lang/RuntimeException", msg);
}

/// Wrapper that catches panics and converts them to Java exceptions.
fn catch_panic_and_throw<F, T>(env: &mut JNIEnv, fallback: T, f: F) -> T
where
    F: FnOnce(&mut JNIEnv) -> T + std::panic::UnwindSafe,
{
    let env_ptr = env as *mut JNIEnv;
    match std::panic::catch_unwind(|| {
        let env = unsafe { &mut *env_ptr };
        f(env)
    }) {
        Ok(result) => result,
        Err(panic_info) => {
            let msg = if let Some(s) = panic_info.downcast_ref::<&str>() {
                format!("Native panic: {}", s)
            } else if let Some(s) = panic_info.downcast_ref::<String>() {
                format!("Native panic: {}", s)
            } else {
                "Native panic: unknown error".to_string()
            };
            throw_runtime_exception(env, &msg);
            fallback
        }
    }
}

// ---------------------------------------------------------------------------
// Stored dialog configuration - created once, shown many times
// ---------------------------------------------------------------------------

/// The type of dialog to show.
enum DialogKind {
    PickFiles { multiple: bool },
    PickDirectory,
    SaveFile { default_name: Option<String> },
}

/// Stores dialog configuration so it can be reused across multiple show() calls.
struct DialogConfig {
    kind: DialogKind,
    title: Option<String>,
    initial_directory: Option<String>,
    extensions: Vec<String>,
}

impl DialogConfig {
    /// Build a fresh FileDialog from this stored config.
    fn build(&self) -> FileDialog {
        let mut dialog = FileDialog::new();

        if let Some(ref t) = self.title {
            dialog = dialog.set_title(t);
        }

        if let Some(ref dir) = self.initial_directory {
            dialog = dialog.set_directory(dir);
        }

        if !self.extensions.is_empty() {
            let ext_refs: Vec<&str> = self.extensions.iter().map(|s| s.as_str()).collect();
            dialog = dialog.add_filter("Selected types", &ext_refs);
        }

        if let DialogKind::SaveFile {
            default_name: Some(ref name),
        } = self.kind
        {
            dialog = dialog.set_file_name(name);
        }

        dialog
    }
}

/// Box a DialogConfig and return its raw pointer as a jlong handle.
fn box_config(config: DialogConfig) -> jlong {
    let boxed = Box::new(config);
    Box::into_raw(boxed) as jlong
}

/// Recover a &DialogConfig from a jlong handle without taking ownership.
///
/// # Safety
/// The handle must be a valid pointer returned by `box_config` that has not been destroyed.
unsafe fn ref_config(handle: jlong) -> &'static DialogConfig {
    &*(handle as *const DialogConfig)
}

/// Recover and free a Box<DialogConfig> from a jlong handle.
///
/// # Safety
/// The handle must be a valid pointer returned by `box_config` and must not be used after this.
unsafe fn drop_config(handle: jlong) {
    let _ = Box::from_raw(handle as *mut DialogConfig);
}

// ---------------------------------------------------------------------------
// JNI exports
// ---------------------------------------------------------------------------

/// No-op — forces the native library to load.
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_init(
    _env: JNIEnv,
    _class: JClass,
) {
}

/// Create a file picker dialog config. Returns a handle (long) to reuse with showFileDialog.
///
/// Signature: (Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Z)J
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_createFileDialog(
    mut env: JNIEnv,
    _class: JClass,
    title: JString,
    initial_directory: JString,
    extensions: JObjectArray,
    multiple: jboolean,
) -> jlong {
    catch_panic_and_throw(&mut env, 0, |env| {
        let config = DialogConfig {
            kind: DialogKind::PickFiles {
                multiple: multiple == JNI_TRUE,
            },
            title: jstring_to_string(env, &title),
            initial_directory: jstring_to_string(env, &initial_directory),
            extensions: if extensions.is_null() {
                Vec::new()
            } else {
                jstring_array_to_vec(env, &extensions)
            },
        };
        box_config(config)
    })
}

/// Create a directory picker dialog config. Returns a handle.
///
/// Signature: (Ljava/lang/String;Ljava/lang/String;)J
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_createDirectoryDialog(
    mut env: JNIEnv,
    _class: JClass,
    title: JString,
    initial_directory: JString,
) -> jlong {
    catch_panic_and_throw(&mut env, 0, |env| {
        let config = DialogConfig {
            kind: DialogKind::PickDirectory,
            title: jstring_to_string(env, &title),
            initial_directory: jstring_to_string(env, &initial_directory),
            extensions: Vec::new(),
        };
        box_config(config)
    })
}

/// Create a save-file dialog config. Returns a handle.
///
/// Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)J
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_createSaveDialog(
    mut env: JNIEnv,
    _class: JClass,
    title: JString,
    initial_directory: JString,
    default_name: JString,
    extension: JString,
) -> jlong {
    catch_panic_and_throw(&mut env, 0, |env| {
        let ext = jstring_to_string(env, &extension);
        let config = DialogConfig {
            kind: DialogKind::SaveFile {
                default_name: jstring_to_string(env, &default_name),
            },
            title: jstring_to_string(env, &title),
            initial_directory: jstring_to_string(env, &initial_directory),
            extensions: ext.into_iter().filter(|e| !e.is_empty()).collect(),
        };
        box_config(config)
    })
}

/// Show a previously created dialog. Returns file paths (pick), directory (pick dir), or save path.
///
/// For file picker: returns String[]
/// Signature: (J)[Ljava/lang/String;
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_showFileDialog(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jobjectArray {
    catch_panic_and_throw(&mut env, std::ptr::null_mut(), |env| {
        let config = unsafe { ref_config(handle) };
        let dialog = config.build();

        let paths: Vec<PathBuf> = match &config.kind {
            DialogKind::PickFiles { multiple: true } => dialog.pick_files().unwrap_or_default(),
            DialogKind::PickFiles { multiple: false } => dialog.pick_file().into_iter().collect(),
            _ => Vec::new(),
        };

        match paths_to_jstring_array(env, &paths) {
            Ok(arr) => arr,
            Err(e) => {
                throw_runtime_exception(env, &format!("Failed to create result array: {}", e));
                std::ptr::null_mut()
            }
        }
    })
}

/// Show a previously created directory dialog. Returns the path or null.
///
/// Signature: (J)Ljava/lang/String;
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_showDirectoryDialog(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jstring {
    catch_panic_and_throw(&mut env, std::ptr::null_mut(), |env| {
        let config = unsafe { ref_config(handle) };
        let dialog = config.build();

        match dialog.pick_folder() {
            Some(path) => path_to_jstring(env, &path),
            None => std::ptr::null_mut(),
        }
    })
}

/// Show a previously created save dialog. Returns the path or null.
///
/// Signature: (J)Ljava/lang/String;
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_showSaveDialog(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jstring {
    catch_panic_and_throw(&mut env, std::ptr::null_mut(), |env| {
        let config = unsafe { ref_config(handle) };
        let dialog = config.build();

        match dialog.save_file() {
            Some(path) => path_to_jstring(env, &path),
            None => std::ptr::null_mut(),
        }
    })
}

/// Destroy a dialog config and free its memory.
///
/// Signature: (J)V
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_destroyDialog(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
) {
    catch_panic_and_throw(&mut env, (), |_env| {
        if handle != 0 {
            unsafe { drop_config(handle) };
        }
    })
}
