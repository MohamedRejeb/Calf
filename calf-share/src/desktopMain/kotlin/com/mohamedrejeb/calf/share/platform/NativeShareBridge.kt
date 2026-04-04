package com.mohamedrejeb.calf.share.platform

/**
 * JNI bridge to the native share library built with Rust.
 *
 * On macOS, this provides a native share sheet via NSSharingServicePicker.
 * On other platforms, [isNativeShareSupported] returns false and
 * the Kotlin desktop launcher falls back to clipboard/Desktop.browse().
 */
internal object NativeShareBridge {

    /** Result code: share completed successfully. */
    const val RESULT_SUCCESS = 0

    /** Result code: user dismissed the share sheet. */
    const val RESULT_DISMISSED = 1

    /** Result code: native sharing is unavailable. */
    const val RESULT_UNAVAILABLE = 2

    init {
        loadNativeLibrary()
    }

    /** No-op — forces the native library to load eagerly. */
    @JvmStatic
    external fun init()

    /** Returns true if native share sheet is supported (macOS only). */
    @JvmStatic
    external fun isNativeShareSupported(): Boolean

    /**
     * Show the native share sheet.
     *
     * @param text Optional text to share.
     * @param url Optional URL to share.
     * @param filePaths Optional array of file paths to share.
     * @param parentWindow The native window handle (NSWindow pointer on macOS).
     * @return One of [RESULT_SUCCESS], [RESULT_DISMISSED], or [RESULT_UNAVAILABLE].
     */
    @JvmStatic
    external fun showShareSheet(
        text: String?,
        url: String?,
        filePaths: Array<String>?,
        parentWindow: Long,
    ): Int
}
