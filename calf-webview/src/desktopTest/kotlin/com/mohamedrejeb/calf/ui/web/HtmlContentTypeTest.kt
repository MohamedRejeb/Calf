package com.mohamedrejeb.calf.ui.web

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * JavaFX's `WebEngine.loadContent(content, contentType)` needs a real content type; a null one left
 * `navigator.loadHtml()` without effect on desktop. Null and blank fall back to HTML.
 */
class HtmlContentTypeTest {
    @Test
    fun `null mime type falls back to html`() {
        assertEquals("text/html", htmlContentType(null))
    }

    @Test
    fun `blank mime type falls back to html`() {
        assertEquals("text/html", htmlContentType("  "))
    }

    @Test
    fun `an explicit mime type is kept`() {
        assertEquals("text/plain", htmlContentType("text/plain"))
    }
}
