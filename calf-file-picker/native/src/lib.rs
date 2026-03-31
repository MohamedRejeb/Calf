use jni::objects::{JClass, JObjectArray, JString};
use jni::sys::{jboolean, jobjectArray, jstring, JNI_TRUE};
use jni::JNIEnv;
use rfd::FileDialog;
use std::path::PathBuf;

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
            // Release the local reference to avoid table overflow on large arrays
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
        // Use lossy conversion as fallback — non-UTF-8 chars become replacement character.
        // This is safer than failing entirely for one bad path.
        let path_str = match path.to_str() {
            Some(s) => s.to_owned(),
            None => path.to_string_lossy().into_owned(),
        };
        let jstr = env.new_string(&path_str)?;
        env.set_object_array_element(&array, i as i32, &jstr)?;
        // Release the local reference — the array holds its own reference
        env.delete_local_ref(jstr)?;
    }

    Ok(array.into_raw())
}

/// Build a FileDialog with common parameters applied.
fn build_dialog(
    env: &mut JNIEnv,
    title: &JString,
    initial_directory: &JString,
    extensions: &JObjectArray,
) -> FileDialog {
    let mut dialog = FileDialog::new();

    if let Some(t) = jstring_to_string(env, title) {
        dialog = dialog.set_title(&t);
    }

    if let Some(dir) = jstring_to_string(env, initial_directory) {
        dialog = dialog.set_directory(&dir);
    }

    if !extensions.is_null() {
        let exts = jstring_array_to_vec(env, extensions);
        if !exts.is_empty() {
            let ext_refs: Vec<&str> = exts.iter().map(|s| s.as_str()).collect();
            dialog = dialog.add_filter("Selected types", &ext_refs);
        }
    }

    dialog
}

/// Convert a path to a jstring, returning null on failure.
/// Uses strict UTF-8 conversion with lossy fallback for non-UTF-8 paths.
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
/// Returns the given `fallback` value if a panic occurs.
fn catch_panic_and_throw<F, T>(env: &mut JNIEnv, fallback: T, f: F) -> T
where
    F: FnOnce(&mut JNIEnv) -> T + std::panic::UnwindSafe,
{
    // We need a raw pointer to pass env through catch_unwind since JNIEnv is not UnwindSafe
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
// JNI exports
// ---------------------------------------------------------------------------

/// JNI: Eagerly initialize the native library (no-op, but forces loading).
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_init(
    _env: JNIEnv,
    _class: JClass,
) {
    // No-op. The library is loaded when this class is first accessed.
    // This function exists so the Kotlin side can trigger loading eagerly.
}

/// JNI: Pick one or more files.
///
/// Signature: (Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Z)[Ljava/lang/String;
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_pickFiles(
    mut env: JNIEnv,
    _class: JClass,
    title: JString,
    initial_directory: JString,
    extensions: JObjectArray,
    multiple: jboolean,
) -> jobjectArray {
    catch_panic_and_throw(&mut env, std::ptr::null_mut(), |env| {
        let dialog = build_dialog(env, &title, &initial_directory, &extensions);

        let paths: Vec<PathBuf> = if multiple == JNI_TRUE {
            dialog.pick_files().unwrap_or_default()
        } else {
            dialog.pick_file().into_iter().collect()
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

/// JNI: Pick a directory.
///
/// Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_pickDirectory(
    mut env: JNIEnv,
    _class: JClass,
    title: JString,
    initial_directory: JString,
) -> jstring {
    catch_panic_and_throw(&mut env, std::ptr::null_mut(), |env| {
        let mut dialog = FileDialog::new();

        if let Some(t) = jstring_to_string(env, &title) {
            dialog = dialog.set_title(&t);
        }

        if let Some(dir) = jstring_to_string(env, &initial_directory) {
            dialog = dialog.set_directory(&dir);
        }

        match dialog.pick_folder() {
            Some(path) => path_to_jstring(env, &path),
            None => std::ptr::null_mut(),
        }
    })
}

/// JNI: Show a save-file dialog.
///
/// Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
#[no_mangle]
pub extern "system" fn Java_com_mohamedrejeb_calf_picker_platform_NativeFilePickerBridge_saveFileDialog(
    mut env: JNIEnv,
    _class: JClass,
    title: JString,
    initial_directory: JString,
    default_name: JString,
    extension: JString,
) -> jstring {
    catch_panic_and_throw(&mut env, std::ptr::null_mut(), |env| {
        let mut dialog = FileDialog::new();

        if let Some(t) = jstring_to_string(env, &title) {
            dialog = dialog.set_title(&t);
        }

        if let Some(dir) = jstring_to_string(env, &initial_directory) {
            dialog = dialog.set_directory(&dir);
        }

        if let Some(name) = jstring_to_string(env, &default_name) {
            dialog = dialog.set_file_name(&name);
        }

        if let Some(ext) = jstring_to_string(env, &extension) {
            if !ext.is_empty() {
                dialog = dialog.add_filter("File", &[&ext]);
            }
        }

        match dialog.save_file() {
            Some(path) => path_to_jstring(env, &path),
            None => std::ptr::null_mut(),
        }
    })
}
