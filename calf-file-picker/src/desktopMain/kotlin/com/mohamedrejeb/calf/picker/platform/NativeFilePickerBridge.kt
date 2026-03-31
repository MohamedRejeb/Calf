package com.mohamedrejeb.calf.picker.platform

/**
 * JNI bridge to the native file picker library built with Rust + rfd.
 */
internal object NativeFilePickerBridge {

    init {
        loadNativeLibrary()
    }

    /**
     * No-op function that forces the native library to load eagerly.
     * Call this early to avoid first-launch latency when [pickFiles] is invoked.
     */
    @JvmStatic
    external fun init()

    /**
     * Open a file picker dialog.
     *
     * @param title Dialog title, or null for default.
     * @param initialDirectory Starting directory path, or null.
     * @param extensions File extensions to filter (e.g. ["png", "jpg"]), or null for all.
     * @param multiple Whether to allow multiple file selection.
     * @return Array of selected file paths, or empty array if cancelled.
     */
    @JvmStatic
    external fun pickFiles(
        title: String?,
        initialDirectory: String?,
        extensions: Array<String>?,
        multiple: Boolean,
    ): Array<String>

    /**
     * Open a directory picker dialog.
     *
     * @param title Dialog title, or null for default.
     * @param initialDirectory Starting directory path, or null.
     * @return Selected directory path, or null if cancelled.
     */
    @JvmStatic
    external fun pickDirectory(
        title: String?,
        initialDirectory: String?,
    ): String?

    /**
     * Open a save-file dialog.
     *
     * @param title Dialog title, or null for default.
     * @param initialDirectory Starting directory path, or null.
     * @param defaultName Default file name (e.g. "document.pdf"), or null.
     * @param extension File extension for the filter (e.g. "pdf"), or null.
     * @return Selected save path, or null if cancelled.
     */
    @JvmStatic
    external fun saveFileDialog(
        title: String?,
        initialDirectory: String?,
        defaultName: String?,
        extension: String?,
    ): String?
}
