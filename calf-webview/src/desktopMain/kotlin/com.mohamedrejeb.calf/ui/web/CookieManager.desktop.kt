package com.mohamedrejeb.calf.ui.web

import java.net.CookieHandler
import java.net.HttpCookie
import java.net.URI

/**
 * Desktop implementation of [CookieManager].
 * Uses [java.net.CookieManager] which is the cookie store used by JavaFX WebEngine.
 */
public actual class CookieManager actual constructor() {

    private val cookieManager: java.net.CookieManager
        get() {
            val handler = CookieHandler.getDefault()
            if (handler is java.net.CookieManager) return handler
            // Install a default CookieManager if none is set
            val newManager = java.net.CookieManager()
            CookieHandler.setDefault(newManager)
            return newManager
        }

    public actual suspend fun setCookie(url: String, cookie: Cookie) {
        val uri = URI(url)
        val httpCookie = HttpCookie(cookie.name, cookie.value).apply {
            domain = cookie.domain ?: uri.host
            path = cookie.path ?: "/"
            secure = cookie.isSecure
            isHttpOnly = cookie.isHttpOnly
            if (cookie.expiresDate != null) {
                val nowSeconds = System.currentTimeMillis() / 1000
                val expiresSeconds = cookie.expiresDate / 1000
                maxAge = (expiresSeconds - nowSeconds).coerceAtLeast(0)
            } else {
                // Session cookie — expires when the JVM exits
                maxAge = -1
            }
        }
        cookieManager.cookieStore.add(uri, httpCookie)
    }

    public actual suspend fun getCookies(url: String): List<Cookie> {
        val uri = URI(url)
        return cookieManager.cookieStore.get(uri).map { httpCookie ->
            Cookie(
                name = httpCookie.name,
                value = httpCookie.value,
                domain = httpCookie.domain,
                path = httpCookie.path,
                expiresDate = if (httpCookie.maxAge > 0) {
                    System.currentTimeMillis() + httpCookie.maxAge * 1000
                } else {
                    null
                },
                isSecure = httpCookie.secure,
                isHttpOnly = httpCookie.isHttpOnly,
            )
        }
    }

    public actual suspend fun removeAllCookies() {
        cookieManager.cookieStore.removeAll()
    }
}
