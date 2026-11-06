package com.mohamedrejeb.calf.io

import android.content.Context
import android.provider.DocumentsContract
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Files from the Storage Access Framework: a picked folder is a bare tree URI, which documents
 * providers refuse to query directly, and its children are tree-plus-document URIs.
 */
@RunWith(RobolectricTestRunner::class)
class KmpFileDocumentsProviderTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = RuntimeEnvironment.getApplication()

    @Before
    fun installProvider() {
        FakeDocumentsProvider.install(tempFolder.root)
        tempFolder.newFolder("Docs")
        tempFolder.newFile("Docs/notes.txt").writeBytes(ByteArray(25))
    }

    private val pickedFolder =
        KmpFile(DocumentsContract.buildTreeDocumentUri(FakeDocumentsProvider.AUTHORITY, "Docs"))

    private val documentInFolder = KmpFile(
        DocumentsContract.buildDocumentUriUsingTree(pickedFolder.uri, "Docs/notes.txt"),
    )

    @Test
    fun getSizeOfAPickedFolderIsNullInsteadOfCrashing() {
        assertNull(pickedFolder.getSize(context))
    }

    @Test
    fun aPickedFolderHasItsNameAndIsADirectory() {
        assertEquals("Docs", pickedFolder.getName(context))
        assertTrue(pickedFolder.isDirectory(context))
        assertEquals(DocumentsContract.Document.MIME_TYPE_DIR, pickedFolder.getMimeType(context))
    }

    @Test
    fun aDocumentInAPickedFolderHasItsSize() {
        assertEquals(25L, documentInFolder.getSize(context))
        assertFalse(documentInFolder.isDirectory(context))
    }

    @Test
    fun getSizeOfAFolderInsideAPickedFolderIsNull() {
        tempFolder.newFolder("Docs", "Archive")
        val folder = KmpFile(DocumentsContract.buildDocumentUriUsingTree(pickedFolder.uri, "Docs/Archive"))

        assertNull(folder.getSize(context))
    }
}
