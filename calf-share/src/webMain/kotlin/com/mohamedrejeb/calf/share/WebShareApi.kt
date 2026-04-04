package com.mohamedrejeb.calf.share

/**
 * Returns `true` if the Web Share API (`navigator.share`) is available in this browser.
 */
internal expect fun isWebShareSupported(): Boolean

/**
 * Shares plain text using the Web Share API.
 * The result callback is invoked with `true` on success, `false` on AbortError (dismissed),
 * or `null` on other errors (e.g., InvalidStateError from a share already in progress).
 *
 * @param text The text to share.
 * @param title Optional title for the share dialog.
 * @param onResult Callback with the share outcome.
 */
internal expect fun webShareText(text: String, title: String?, onResult: (Boolean?) -> Unit)

/**
 * Shares a URL using the Web Share API.
 *
 * @param url The URL to share.
 * @param title Optional title for the share dialog.
 * @param onResult Callback with the share outcome.
 */
internal expect fun webShareUrl(url: String, title: String?, onResult: (Boolean?) -> Unit)

/**
 * Shares a file using the Web Share API.
 *
 * @param file The web File object to share.
 * @param onResult Callback with the share outcome.
 */
internal expect fun webShareFile(file: org.w3c.files.File, onResult: (Boolean?) -> Unit)

/**
 * Shares multiple files using the Web Share API.
 *
 * @param files The web File objects to share.
 * @param onResult Callback with the share outcome.
 */
internal expect fun webShareFiles(files: List<org.w3c.files.File>, onResult: (Boolean?) -> Unit)

/**
 * Copies the given text to the system clipboard.
 *
 * @param text The text to copy.
 */
internal expect fun copyToClipboard(text: String)
