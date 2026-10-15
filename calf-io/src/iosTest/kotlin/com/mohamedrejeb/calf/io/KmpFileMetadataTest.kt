package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithLength
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalForeignApi::class)
class KmpFileMetadataTest {
    private val context = PlatformContext.INSTANCE
    private val directory = "${NSTemporaryDirectory().removeSuffix("/")}/calf-io-metadata-${NSUUID().UUIDString}"

    init {
        NSFileManager.defaultManager.createDirectoryAtPath(
            directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
    }

    @AfterTest
    fun deleteDirectory() {
        NSFileManager.defaultManager.removeItemAtPath(directory, error = null)
    }

    @Test
    fun fromUriStringReadsBackToUriString() {
        val file = KmpFile(NSURL.fileURLWithPath("$directory/Report Final.pdf"))

        val restored = assertNotNull(KmpFile.fromUriString(assertNotNull(file.toUriString())))

        assertEquals("$directory/Report Final.pdf", restored.getPath(context))
    }

    @Test
    fun fromUriStringAcceptsAPlainPath() {
        val restored = assertNotNull(KmpFile.fromUriString("/tmp/Report Final.pdf"))

        assertEquals("/tmp/Report Final.pdf", restored.getPath(context))
    }

    @Test
    fun fromUriStringRejectsWhatIsNotAFile() {
        assertNull(KmpFile.fromUriString("https://example.com/Report.pdf"))
        assertNull(KmpFile.fromUriString("relative/path.pdf"))
        assertNull(KmpFile.fromUriString(""))
    }

    @Test
    fun getSizeIsTheNumberOfBytes() {
        val url = NSURL.fileURLWithPath("$directory/data.bin")
        NSFileManager.defaultManager.createFileAtPath(
            path = assertNotNull(url.path),
            contents = NSMutableData.dataWithLength(1_234u),
            attributes = null,
        )

        assertEquals(1_234L, KmpFile(url).getSize(context))
    }

    @Test
    fun getSizeOfAMissingFileOrADirectoryIsNull() {
        assertNull(KmpFile(NSURL.fileURLWithPath("$directory/missing.bin")).getSize(context))
        assertNull(KmpFile(NSURL.fileURLWithPath(directory, isDirectory = true)).getSize(context))
    }

    // The extension lookup goes through the system's Uniform Type database, which the simulator
    // test process does not load: even UTTypePDF reports no MIME type there. Only the cases that
    // do not reach the database are checked here.
    @Test
    fun getMimeTypeOfAFileWithoutAnExtensionIsNull() {
        assertNull(KmpFile(NSURL.fileURLWithPath("$directory/README")).getMimeType(context))
    }
}
