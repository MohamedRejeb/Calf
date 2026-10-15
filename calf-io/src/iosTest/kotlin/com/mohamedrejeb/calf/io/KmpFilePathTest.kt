package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Callers hand [getPath] to kotlinx-io, PDFium and friends, so it must be a decoded file system
 * path, never a `file://` URL string with percent escapes.
 */
class KmpFilePathTest {
    private val context = PlatformContext.INSTANCE

    @Test
    fun getPathOfAFileUrlIsTheDecodedFileSystemPath() {
        val file = KmpFile(NSURL.fileURLWithPath("/private/var/mobile/Report Final.pdf"))
        assertEquals("/private/var/mobile/Report Final.pdf", file.getPath(context))
    }

    @Test
    fun getPathOfANonFileUrlIsNull() {
        val file = KmpFile(NSURL(string = "https://example.com/Report.pdf"))
        assertNull(file.getPath(context))
    }

    @Test
    fun getNameIsDecoded() {
        val file = KmpFile(NSURL.fileURLWithPath("/tmp/Report Final.pdf"))
        assertEquals("Report Final.pdf", file.getName(context))
    }

    @Test
    fun getNameOfADirectoryHasNoTrailingSlash() {
        val directory = KmpFile(NSURL.fileURLWithPath("/tmp/Exports", isDirectory = true))
        assertEquals("Exports", directory.getName(context))
    }

    @Test
    fun toUriStringIsTheEncodedUrl() {
        val file = KmpFile(NSURL.fileURLWithPath("/tmp/Report Final.pdf"))
        assertEquals("file:///tmp/Report%20Final.pdf", file.toUriString())
    }
}
