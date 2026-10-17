package com.mohamedrejeb.calf.picker

import android.net.Uri
import com.mohamedrejeb.calf.io.KmpFile
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

/**
 * Android's pickers open at a URI, and a picked folder is a `content:` tree URI with no file
 * system path, so a folder given as a [KmpFile] is carried as its URI.
 */
@RunWith(RobolectricTestRunner::class)
class InitialDirectoryTest {
    @Test
    fun aPickedFolderBecomesItsTreeUri() {
        val tree = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")

        val settings = FilePickerSettings(initialDirectory = KmpFile(tree))

        assertEquals(tree.toString(), settings.initialDirectory)
    }

    @Test
    fun aFileFolderBecomesItsUri() {
        val folder = Uri.parse("file:///storage/emulated/0/Documents")

        val settings = FilePickerSettings(initialDirectory = KmpFile(folder))

        assertEquals("file:///storage/emulated/0/Documents", settings.initialDirectory)
    }
}
