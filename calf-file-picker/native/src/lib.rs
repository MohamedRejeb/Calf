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

/// Convert a Java String[] to Vec<String>.
fn jstring_array_to_vec(env: &mut JNIEnv, array: &JObjectArray) -> Vec<String> {
    let len = env.get_array_length(array).unwrap_or(0);
    let mut result = Vec::with_capacity(len as usize);
    for i in 0..len {
        if let Ok(obj) = env.get_object_array_element(array, i) {
            let jstr: JString = obj.into();
            if let Some(s) = jstring_to_string(env, &jstr) {
                result.push(s);
            }
        }
    }
    result
}

/// Convert a Vec of paths to a Java String[].
fn paths_to_jstring_array(env: &mut JNIEnv, paths: &[PathBuf]) -> jobjectArray {
    let string_class = env.find_class("java/lang/String").unwrap();
    let array = env
        .new_object_array(paths.len() as i32, &string_class, JString::default())
        .unwrap();
    for (i, path) in paths.iter().enumerate() {
        let path_str = path.to_string_lossy();
        let jstr = env.new_string(&*path_str).unwrap();
        env.set_object_array_element(&array, i as i32, jstr)
            .unwrap();
    }
    array.into_raw()
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
    let dialog = build_dialog(&mut env, &title, &initial_directory, &extensions);

    let paths: Vec<PathBuf> = if multiple == JNI_TRUE {
        dialog.pick_files().unwrap_or_default()
    } else {
        dialog.pick_file().into_iter().collect()
    };

    paths_to_jstring_array(&mut env, &paths)
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
    let mut dialog = FileDialog::new();

    if let Some(t) = jstring_to_string(&mut env, &title) {
        dialog = dialog.set_title(&t);
    }

    if let Some(dir) = jstring_to_string(&mut env, &initial_directory) {
        dialog = dialog.set_directory(&dir);
    }

    match dialog.pick_folder() {
        Some(path) => {
            let path_str = path.to_string_lossy();
            env.new_string(&*path_str)
                .map(|s| s.into_raw())
                .unwrap_or(std::ptr::null_mut())
        }
        None => std::ptr::null_mut(),
    }
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
    let mut dialog = FileDialog::new();

    if let Some(t) = jstring_to_string(&mut env, &title) {
        dialog = dialog.set_title(&t);
    }

    if let Some(dir) = jstring_to_string(&mut env, &initial_directory) {
        dialog = dialog.set_directory(&dir);
    }

    if let Some(name) = jstring_to_string(&mut env, &default_name) {
        dialog = dialog.set_file_name(&name);
    }

    if let Some(ext) = jstring_to_string(&mut env, &extension) {
        if !ext.is_empty() {
            dialog = dialog.add_filter("File", &[&ext]);
        }
    }

    match dialog.save_file() {
        Some(path) => {
            let path_str = path.to_string_lossy();
            env.new_string(&*path_str)
                .map(|s| s.into_raw())
                .unwrap_or(std::ptr::null_mut())
        }
        None => std::ptr::null_mut(),
    }
}
