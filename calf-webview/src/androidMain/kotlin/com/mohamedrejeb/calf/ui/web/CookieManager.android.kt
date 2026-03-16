package com.mohamedrejeb.calf.ui.web

import android.webkit.CookieManager as AndroidCookieManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Android implementation of [CookieManager] using [android.webkit.CookieManager].
 */
public actual class CookieManager actual constructor() {

    private val cookieManager: AndroidCookieManager
        get() = AndroidCookieManager.getInstance()

    /**
     * Sets a cookie for the given URL.
     *
     * The cookie is converted to a Set-Cookie header string and passed to
     * [AndroidCookieManager.setCookie].
     *
     * @param url The URL for which the cookie is to be set.
     * @param cookie The [Cookie] to set.
     */
    public actual suspend fun setCookie(url: String, cookie: Cookie) {
        withContext(Dispatchers.Main) {
            val cookieString = buildCookieString(cookie)
            suspendCancellableCoroutine { continuation ->
                cookieManager.setCookie(url, cookieString) {
                    continuation.resume(Unit)
                }
            }
            cookieManager.flush()
        }
    }

    /**
     * Gets all cookies for the given URL.
     *
     * @param url The URL for which cookies are to be retrieved.
     * @return A list of [Cookie] objects associated with the URL.
     */
    public actual suspend fun getCookies(url: String): List<Cookie> {
        return withContext(Dispatchers.Main) {
            val cookieString = cookieManager.getCookie(url) ?: return@withContext emptyList()
            parseCookies(cookieString)
        }
    }

    /**
     * Removes all cookies.
     */
    public actual suspend fun removeAllCookies() {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                cookieManager.removeAllCookies {
                    continuation.resume(Unit)
                }
            }
            cookieManager.flush()
        }
    }
}

/**
 * Builds a Set-Cookie header string from a [Cookie] object.
 */
private fun buildCookieString(cookie: Cookie): String = buildString {
    append("${cookie.name}=${cookie.value}")
    cookie.domain?.let { append("; Domain=$it") }
    cookie.path?.let { append("; Path=$it") }
    cookie.expiresDate?.let { append("; Max-Age=${(it - System.currentTimeMillis()) / 1000}") }
    if (cookie.isSecure) append("; Secure")
    if (cookie.isHttpOnly) append("; HttpOnly")
}

/**
 * Parses a cookie header string (as returned by [AndroidCookieManager.getCookie])
 * into a list of [Cookie] objects.
 *
 * The cookie string format is "name1=value1; name2=value2; ...".
 */
private fun parseCookies(cookieString: String): List<Cookie> {
    println("cookieString: $cookieString")
    return cookieString.split(";")
        .mapNotNull { part ->
            val trimmed = part.trim()
            val equalsIndex = trimmed.indexOf('=')
            if (equalsIndex > 0) {
                Cookie(
                    name = trimmed.substring(0, equalsIndex),
                    value = trimmed.substring(equalsIndex + 1),
                )
            } else {
                null
            }
        }
}
