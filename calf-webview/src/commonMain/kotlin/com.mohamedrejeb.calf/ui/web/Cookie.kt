package com.mohamedrejeb.calf.ui.web

/**
 * Represents an HTTP cookie.
 *
 * @param name The name of the cookie.
 * @param value The value of the cookie.
 * @param domain The domain for which the cookie is valid.
 * @param path The path for which the cookie is valid.
 * @param expiresDate The expiration date of the cookie in milliseconds since epoch.
 *                    If null, the cookie is a session cookie.
 * @param isSecure Whether the cookie should only be sent over secure (HTTPS) connections.
 * @param isHttpOnly Whether the cookie is HTTP-only (not accessible via JavaScript).
 */
public data class Cookie(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val expiresDate: Long? = null,
    val isSecure: Boolean = false,
    val isHttpOnly: Boolean = false,
)
