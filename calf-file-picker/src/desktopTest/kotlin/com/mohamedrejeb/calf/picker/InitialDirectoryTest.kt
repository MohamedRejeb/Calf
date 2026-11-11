package com.mohamedrejeb.calf.picker

import com.mohamedrejeb.calf.io.KmpFile
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** On Desktop the dialogs open at a path, so a folder given as a [KmpFile] becomes its path. */
class InitialDirectoryTest {
    private val folder = File("/tmp/Calf Exports")

    @Test
    fun aFolderBecomesItsPath() {
        val settings = FilePickerSettings(initialDirectory = KmpFile(folder))
        assertEquals(folder.path, settings.initialDirectory)
    }

    @Test
    fun noFolderLeavesTheInitialDirectoryUnset() {
        val folder: KmpFile? = null
        assertNull(FilePickerSettings(initialDirectory = folder).initialDirectory)
    }

    @Test
    fun theOtherSettingsAreKept() {
        val settings = FilePickerSettings(
            initialDirectory = KmpFile(folder),
            title = "Pick a report",
            imageRepresentationMode = ImageRepresentationMode.Current,
        )
        assertEquals("Pick a report", settings.title)
        assertEquals(ImageRepresentationMode.Current, settings.imageRepresentationMode)
    }

    @Test
    fun aStringStillWorksAndAnEmptyCallStillCompiles() {
        assertEquals("/home/user", FilePickerSettings(initialDirectory = "/home/user").initialDirectory)
        assertNull(FilePickerSettings().initialDirectory)
    }
}
