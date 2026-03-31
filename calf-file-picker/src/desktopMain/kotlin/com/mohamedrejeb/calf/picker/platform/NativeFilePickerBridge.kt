package com.mohamedrejeb.calf.picker.platform

/**
 * JNI bridge to the native file picker library built with Rust + rfd.
 */
internal object NativeFilePickerBridge {

    init {
        loadNativeLibrary()
    }

    /** No-op - forces the native library to load eagerly. */
    @JvmStatic
    external fun init()

    // -- Dialog creation (returns a handle for reuse) --

    /**
     * Create a file picker dialog config stored in native memory.
     * @return A handle (pointer) to pass to [showFileDialog] / [destroyDialog].
     */
    @JvmStatic
    external fun createFileDialog(
        title: String?,
        initialDirectory: String?,
        extensions: Array<String>?,
        multiple: Boolean,
    ): Long

    /**
     * Create a directory picker dialog config stored in native memory.
     * @return A handle to pass to [showDirectoryDialog] / [destroyDialog].
     */
    @JvmStatic
    external fun createDirectoryDialog(
        title: String?,
        initialDirectory: String?,
    ): Long

    /**
     * Create a save-file dialog config stored in native memory.
     * @return A handle to pass to [showSaveDialog] / [destroyDialog].
     */
    @JvmStatic
    external fun createSaveDialog(
        title: String?,
        initialDirectory: String?,
        defaultName: String?,
        extension: String?,
    ): Long

    // -- Show dialog (reuses a previously created handle) --

    /**
     * Show a file picker dialog using a previously created handle.
     * @return Array of selected file paths, or empty array if cancelled.
     */
    @JvmStatic
    external fun showFileDialog(handle: Long): Array<String>

    /**
     * Show a directory picker dialog using a previously created handle.
     * @return Selected directory path, or null if cancelled.
     */
    @JvmStatic
    external fun showDirectoryDialog(handle: Long): String?

    /**
     * Show a save-file dialog using a previously created handle.
     * @return Selected save path, or null if cancelled.
     */
    @JvmStatic
    external fun showSaveDialog(handle: Long): String?

    // -- Cleanup --

    /**
     * Free the native memory for a dialog config.
     * Must be called when the dialog is no longer needed.
     */
    @JvmStatic
    external fun destroyDialog(handle: Long)
}
