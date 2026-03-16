package com.mohamedrejeb.calf.ui.web

import kotlinx.browser.document

/**
 * Web implementation of [CookieManager].
 * Uses the browser's `document.cookie` API to manage cookies for the host page domain.
 *
 * Note: This only manages cookies for the host page's domain, not for cross-origin iframe content.
 */
public actual class CookieManager actual constructor() {
    public actual suspend fun setCookie(url: String, cookie: Cookie) {
        val cookieString = buildString {
            append("${cookie.name}=${cookie.value}")
            cookie.domain?.let { append("; domain=$it") }
            cookie.path?.let { append("; path=$it") }
            cookie.expiresDate?.let { append("; expires=$it") }
            if (cookie.isSecure) append("; Secure")
            if (cookie.isHttpOnly) append("; HttpOnly")
        }
        document.cookie = cookieString
    }

    public actual suspend fun getCookies(url: String): List<Cookie> {
        val cookieString = document.cookie
        if (cookieString.isBlank()) return emptyList()
        return cookieString.split(";").mapNotNull { part ->
            val trimmed = part.trim()
            val eqIndex = trimmed.indexOf('=')
            if (eqIndex > 0) {
                Cookie(
                    name = trimmed.substring(0, eqIndex).trim(),
                    value = trimmed.substring(eqIndex + 1).trim(),
                )
            } else {
                null
            }
        }
    }

    public actual suspend fun removeAllCookies() {
        val cookies = getCookies("")
        for (cookie in cookies) {
            document.cookie = "${cookie.name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/"
        }
    }
}
