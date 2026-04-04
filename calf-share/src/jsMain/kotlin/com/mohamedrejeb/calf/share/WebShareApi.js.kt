package com.mohamedrejeb.calf.share

import kotlinx.browser.window

internal actual fun isWebShareSupported(): Boolean {
    return js("('share' in navigator)").unsafeCast<Boolean>()
}

internal actual fun webShareText(text: String, title: String?, onResult: (Boolean?) -> Unit) {
    val data = js("{}")
    data.text = text
    if (title != null) data.title = title
    handleSharePromise(window.navigator.asDynamic().share(data), onResult)
}

internal actual fun webShareUrl(url: String, title: String?, onResult: (Boolean?) -> Unit) {
    val data = js("{}")
    data.url = url
    if (title != null) data.title = title
    handleSharePromise(window.navigator.asDynamic().share(data), onResult)
}

internal actual fun webShareFile(file: org.w3c.files.File, onResult: (Boolean?) -> Unit) {
    val data = js("{}")
    data.files = arrayOf(file)
    handleSharePromise(window.navigator.asDynamic().share(data), onResult)
}

internal actual fun webShareFiles(files: List<org.w3c.files.File>, onResult: (Boolean?) -> Unit) {
    val data = js("{}")
    data.files = files.toTypedArray()
    handleSharePromise(window.navigator.asDynamic().share(data), onResult)
}

internal actual fun copyToClipboard(text: String) {
    window.navigator.asDynamic().clipboard.writeText(text)
}

/**
 * Handles the Promise returned by navigator.share().
 * - resolve → onResult(true) (share completed)
 * - reject with AbortError → onResult(false) (user dismissed)
 * - reject with other error → onResult(null) (e.g., InvalidStateError, NotAllowedError)
 */
private fun handleSharePromise(promise: dynamic, onResult: (Boolean?) -> Unit) {
    promise.then {
        onResult(true)
    }.catch { error: dynamic ->
        val errorName = error?.name as? String
        if (errorName == "AbortError") {
            onResult(false)
        } else {
            onResult(null)
        }
    }
}
