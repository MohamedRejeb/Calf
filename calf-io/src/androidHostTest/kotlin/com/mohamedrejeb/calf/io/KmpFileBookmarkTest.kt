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
import kotlin.test.assertTrue

/**
 * A picked `content:` URI stays readable after a restart only with a persisted permission, which
 * the bookmark takes and releases.
 */
@RunWith(RobolectricTestRunner::class)
class KmpFileBookmarkTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = RuntimeEnvironment.getApplication()

    private val tree = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")

    @Test
    fun aBookmarkPersistsThePermissionAndReopensTheFolder() {
        val bookmark = assertNotNull(KmpFile(tree).createBookmark(context))

        assertTrue(context.contentResolver.persistedUriPermissions.any { it.uri == tree })
        assertEquals(tree, KmpFile.fromBookmark(bookmark, context)?.uri)
    }

    @Test
    fun releasingTheBookmarkGivesThePermissionUp() {
        val file = KmpFile(tree)
        val bookmark = assertNotNull(file.createBookmark(context))

        file.releaseBookmark(context)

        assertTrue(context.contentResolver.persistedUriPermissions.none { it.uri == tree })
        assertNull(KmpFile.fromBookmark(bookmark, context), "access was given up")
    }

    @Test
    fun aContentUriWithoutAPersistedPermissionDoesNotReopen() {
        assertNull(KmpFile.fromBookmark(tree.toString(), context))
    }

    @Test
    fun aFileBookmarkReopensWhileTheFileExists() {
        val file = tempFolder.newFile("Report.pdf")
        val bookmark = assertNotNull(KmpFile(Uri.fromFile(file)).createBookmark(context))

        assertEquals(Uri.fromFile(file), KmpFile.fromBookmark(bookmark, context)?.uri)

        file.delete()
        assertNull(KmpFile.fromBookmark(bookmark, context))
    }

    @Test
    fun somethingThatIsNotABookmarkDoesNotReopen() {
        assertNull(KmpFile.fromBookmark("https://example.com/Report.pdf", context))
        assertNull(KmpFile.fromBookmark("", context))
    }
}
