package com.mohamedrejeb.calf.ui.web

/**
 * WasmJS implementation of [CookieManager].
 * Not yet implemented — all methods are no-ops.
 */
public actual class CookieManager actual constructor() {
    public actual suspend fun setCookie(url: String, cookie: Cookie) {}

    public actual suspend fun getCookies(url: String): List<Cookie> = emptyList()

    public actual suspend fun removeAllCookies() {}
}
