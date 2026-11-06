package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import java.io.File
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KmpFilePathTest {
    private val context = PlatformContext.INSTANCE

    @Test
    fun getPathIsTheFileSystemPath() {
        val file = File("/tmp/Report Final.pdf")
        assertEquals(file.path, KmpFile(file).getPath(context))
    }

    @Test
    fun toUriStringIsAnEncodedFileUriOfTheSameFile() {
        val file = File("/tmp/Report Final.pdf").absoluteFile
        val uri = URI(requireNotNull(KmpFile(file).toUriString()))
        assertEquals("file", uri.scheme)
        assertEquals(file, File(uri))
        assertTrue(uri.rawPath.endsWith("Report%20Final.pdf"))
    }
}
