package com.mohamedrejeb.calf.ui.web

/**
 * The content type handed to JavaFX's `WebEngine.loadContent(content, contentType)`. The engine does
 * nothing with a null type, so a missing or blank [mimeType] means HTML.
 */
internal fun htmlContentType(mimeType: String?): String =
    mimeType?.takeIf { it.isNotBlank() } ?: HTML_CONTENT_TYPE

private const val HTML_CONTENT_TYPE = "text/html"
