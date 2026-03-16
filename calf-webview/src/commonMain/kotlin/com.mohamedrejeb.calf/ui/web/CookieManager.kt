package com.mohamedrejeb.calf.ui.web

/**
 * A cross-platform cookie manager for managing cookies in the WebView.
 *
 * Each platform wraps its native cookie management API:
 * - Android: `android.webkit.CookieManager`
 * - iOS: `WKHTTPCookieStore`
 * - Desktop: `java.net.CookieManager` (used by JavaFX WebEngine)
 * - JS/WasmJS: No-op (not yet implemented)
 */
public expect class CookieManager() {
    /**
     * Sets a cookie for the given URL.
     *
     * @param url The URL for which the cookie is to be set.
     * @param cookie The [Cookie] to set.
     */
    public suspend fun setCookie(url: String, cookie: Cookie)

    /**
     * Gets all cookies for the given URL.
     *
     * @param url The URL for which cookies are to be retrieved.
     * @return A list of [Cookie] objects associated with the URL.
     */
    public suspend fun getCookies(url: String): List<Cookie>

    /**
     * Removes all cookies.
     */
    public suspend fun removeAllCookies()
}
