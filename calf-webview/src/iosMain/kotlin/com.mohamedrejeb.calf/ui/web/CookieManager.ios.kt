package com.mohamedrejeb.calf.ui.web

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSHTTPCookieDomain
import platform.Foundation.NSHTTPCookieExpires
import platform.Foundation.NSHTTPCookieName
import platform.Foundation.NSHTTPCookiePath
import platform.Foundation.NSHTTPCookieSecure
import platform.Foundation.NSHTTPCookieValue
import platform.Foundation.NSURL
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.WebKit.WKHTTPCookieStore
import platform.WebKit.WKWebsiteDataStore
import kotlin.coroutines.resume

/**
 * iOS implementation of [CookieManager] using [WKHTTPCookieStore].
 *
 * Uses the default [WKWebsiteDataStore]'s HTTP cookie store to manage cookies.
 */
public actual class CookieManager actual constructor() {

    private val httpCookieStore: WKHTTPCookieStore
        get() = WKWebsiteDataStore.defaultDataStore().httpCookieStore

    /**
     * Sets a cookie for the given URL using [WKHTTPCookieStore.setCookie].
     *
     * @param url The URL for which the cookie is to be set.
     * @param cookie The [Cookie] to set.
     */
    public actual suspend fun setCookie(url: String, cookie: Cookie) {
        val properties = mutableMapOf<Any?, Any?>()
        properties[NSHTTPCookieName] = cookie.name
        properties[NSHTTPCookieValue] = cookie.value
        properties[NSHTTPCookiePath] = cookie.path ?: "/"

        val domain = cookie.domain ?: NSURL(string = url).host
        if (domain != null) {
            properties[NSHTTPCookieDomain] = domain
        }

        cookie.expiresDate?.let {
            val nsDate = NSDate.dateWithTimeIntervalSince1970(it / 1000.0)
            properties[NSHTTPCookieExpires] = nsDate
        }

        if (cookie.isSecure) {
            properties[NSHTTPCookieSecure] = "TRUE"
        }

        if (cookie.isHttpOnly) {
            properties["HttpOnly"] = "TRUE"
        }

        val nsCookie = NSHTTPCookie.cookieWithProperties(properties) ?: return

        suspendCancellableCoroutine { continuation ->
            httpCookieStore.setCookie(nsCookie) {
                continuation.resume(Unit)
            }
        }
    }

    /**
     * Gets all cookies matching the given URL's domain from [WKHTTPCookieStore].
     *
     * @param url The URL for which cookies are to be retrieved.
     * @return A list of [Cookie] objects associated with the URL.
     */
    @Suppress("UNCHECKED_CAST")
    public actual suspend fun getCookies(url: String): List<Cookie> {
        val nsUrl = NSURL(string = url)
        val host = nsUrl.host ?: return emptyList()

        val allCookies = suspendCancellableCoroutine { continuation ->
            httpCookieStore.getAllCookies { cookies ->
                continuation.resume(cookies as? List<NSHTTPCookie> ?: emptyList())
            }
        }

        return allCookies
            .filter { nsCookie ->
                val cookieDomain = nsCookie.domain
                host == cookieDomain ||
                    host.endsWith(".$cookieDomain") ||
                    (cookieDomain.startsWith(".") && host.endsWith(cookieDomain)) ||
                    (cookieDomain.startsWith(".") && host == cookieDomain.removePrefix("."))
            }
            .map { nsCookie ->
                Cookie(
                    name = nsCookie.name,
                    value = nsCookie.value,
                    domain = nsCookie.domain,
                    path = nsCookie.path,
                    expiresDate = nsCookie.expiresDate?.timeIntervalSince1970?.let { (it * 1000).toLong() },
                    isSecure = nsCookie.isSecure(),
                    isHttpOnly = nsCookie.isHTTPOnly(),
                )
            }
    }

    /**
     * Removes all cookies from [WKHTTPCookieStore].
     */
    @Suppress("UNCHECKED_CAST")
    public actual suspend fun removeAllCookies() {
        val allCookies = suspendCancellableCoroutine { continuation ->
            httpCookieStore.getAllCookies { cookies ->
                continuation.resume(cookies as? List<NSHTTPCookie> ?: emptyList())
            }
        }

        allCookies.forEach { nsCookie ->
            suspendCancellableCoroutine { continuation ->
                httpCookieStore.deleteCookie(nsCookie) {
                    continuation.resume(Unit)
                }
            }
        }
    }
}
