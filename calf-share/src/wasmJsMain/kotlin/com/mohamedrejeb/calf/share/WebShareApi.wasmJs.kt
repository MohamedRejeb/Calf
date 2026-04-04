@file:OptIn(ExperimentalWasmJsInterop::class)

package com.mohamedrejeb.calf.share

import kotlin.js.ExperimentalWasmJsInterop

@JsFun("function() { return ('share' in navigator); }")
private external fun jsIsShareSupported(): Boolean

@JsFun("""
function(text, title, onSuccess, onDismissed, onError) {
    var data = { text: text };
    if (title) data.title = title;
    navigator.share(data).then(function() {
        onSuccess();
    }).catch(function(e) {
        if (e && e.name === 'AbortError') { onDismissed(); } else { onError(); }
    });
}
""")
private external fun jsShareText(
    text: String,
    title: JsString?,
    onSuccess: () -> Unit,
    onDismissed: () -> Unit,
    onError: () -> Unit,
)

@JsFun("""
function(url, title, onSuccess, onDismissed, onError) {
    var data = { url: url };
    if (title) data.title = title;
    navigator.share(data).then(function() {
        onSuccess();
    }).catch(function(e) {
        if (e && e.name === 'AbortError') { onDismissed(); } else { onError(); }
    });
}
""")
private external fun jsShareUrl(
    url: String,
    title: JsString?,
    onSuccess: () -> Unit,
    onDismissed: () -> Unit,
    onError: () -> Unit,
)

@JsFun("""
function(file, onSuccess, onDismissed, onError) {
    navigator.share({ files: [file] }).then(function() {
        onSuccess();
    }).catch(function(e) {
        if (e && e.name === 'AbortError') { onDismissed(); } else { onError(); }
    });
}
""")
private external fun jsShareFile(
    file: JsAny,
    onSuccess: () -> Unit,
    onDismissed: () -> Unit,
    onError: () -> Unit,
)

@JsFun("""
function(files, onSuccess, onDismissed, onError) {
    navigator.share({ files: Array.from(files) }).then(function() {
        onSuccess();
    }).catch(function(e) {
        if (e && e.name === 'AbortError') { onDismissed(); } else { onError(); }
    });
}
""")
private external fun jsShareFiles(
    files: JsArray<JsAny>,
    onSuccess: () -> Unit,
    onDismissed: () -> Unit,
    onError: () -> Unit,
)

@JsFun("function(text) { navigator.clipboard.writeText(text); }")
private external fun jsCopyToClipboard(text: String)

internal actual fun isWebShareSupported(): Boolean =
    jsIsShareSupported()

internal actual fun webShareText(text: String, title: String?, onResult: (Boolean?) -> Unit) {
    jsShareText(
        text = text,
        title = title?.toJsString(),
        onSuccess = { onResult(true) },
        onDismissed = { onResult(false) },
        onError = { onResult(null) },
    )
}

internal actual fun webShareUrl(url: String, title: String?, onResult: (Boolean?) -> Unit) {
    jsShareUrl(
        url = url,
        title = title?.toJsString(),
        onSuccess = { onResult(true) },
        onDismissed = { onResult(false) },
        onError = { onResult(null) },
    )
}

internal actual fun webShareFile(file: org.w3c.files.File, onResult: (Boolean?) -> Unit) {
    jsShareFile(
        file = file as JsAny,
        onSuccess = { onResult(true) },
        onDismissed = { onResult(false) },
        onError = { onResult(null) },
    )
}

internal actual fun webShareFiles(files: List<org.w3c.files.File>, onResult: (Boolean?) -> Unit) {
    val jsArray = JsArray<JsAny>()
    files.forEach { jsArrayPush(jsArray, it as JsAny) }
    jsShareFiles(
        files = jsArray,
        onSuccess = { onResult(true) },
        onDismissed = { onResult(false) },
        onError = { onResult(null) },
    )
}

internal actual fun copyToClipboard(text: String) {
    jsCopyToClipboard(text)
}

@JsFun("function(array, item) { array.push(item); }")
private external fun jsArrayPush(array: JsArray<JsAny>, item: JsAny)
