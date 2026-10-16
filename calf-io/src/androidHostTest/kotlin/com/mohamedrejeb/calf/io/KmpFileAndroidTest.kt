package com.mohamedrejeb.calf.io

import android.content.Context
import android.net.Uri
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class KmpFileAndroidTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = RuntimeEnvironment.getApplication()

    private val contentUri =
        Uri.parse("content://com.android.providers.downloads.documents/document/msf%3A42")

    @Test
    fun aContentUriHasNoFileSystemPath() {
        assertNull(KmpFile(contentUri).getPath(context))
    }

    @Test
    fun aFileUriHasItsDecodedPath() {
        val file = KmpFile(Uri.parse("file:///storage/emulated/0/Download/Report%20Final.pdf"))

        assertEquals("/storage/emulated/0/Download/Report Final.pdf", file.getPath(context))
    }

    @Test
    fun fromUriStringReadsBackAContentUri() {
        val restored = assertNotNull(KmpFile.fromUriString(assertNotNull(KmpFile(contentUri).toUriString())))

        assertEquals(contentUri, restored.uri)
    }

    @Test
    fun fromUriStringAcceptsAPlainPath() {
        val restored = assertNotNull(KmpFile.fromUriString("/storage/emulated/0/Download/Report Final.pdf"))

        assertEquals("file", restored.uri.scheme)
        assertEquals("/storage/emulated/0/Download/Report Final.pdf", restored.getPath(context))
    }

    @Test
    fun fromUriStringRejectsWhatIsNotAFile() {
        assertNull(KmpFile.fromUriString("https://example.com/Report.pdf"))
        assertNull(KmpFile.fromUriString("relative/path.pdf"))
        assertNull(KmpFile.fromUriString(""))
    }

    @Test
    fun getSizeOfAFileUriIsTheNumberOfBytes() {
        val file = tempFolder.newFile("data.bin").apply { writeBytes(ByteArray(1_234)) }

        assertEquals(1_234L, KmpFile(Uri.fromFile(file)).getSize(context))
    }

    @Test
    fun getSizeOfAMissingFileIsNull() {
        val missing = tempFolder.root.resolve("missing.bin")

        assertNull(KmpFile(Uri.fromFile(missing)).getSize(context))
    }

    @Test
    fun getMimeTypeOfAFileUriFollowsTheExtension() {
        assertEquals("application/pdf", KmpFile(Uri.fromFile(tempFolder.root.resolve("Report.PDF"))).getMimeType(context))
        assertNull(KmpFile(Uri.fromFile(tempFolder.root.resolve("data.calfunknown"))).getMimeType(context))
    }
}
