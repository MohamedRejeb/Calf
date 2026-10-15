package com.mohamedrejeb.calf.picker

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Picked files are copied while the picker still grants access. The copy keeps the picked file's
 * name, so its path is as meaningful as the original's, and two picks of the same name never
 * collide.
 */
@OptIn(ExperimentalForeignApi::class)
class PickedFileStagingTest {
    @Test
    fun theCopyKeepsThePickedFilesName() {
        val source = givenFile("Report Final.pdf")

        val copy = assertNotNull(stagePickedFile(source))

        assertEquals("Report Final.pdf", copy.lastPathComponent)
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(assertNotNull(copy.path)))
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(assertNotNull(source.path)), "the source is left in place")
        removeStaging(source)
        removeStaging(copy)
    }

    @Test
    fun twoPicksOfTheSameNameDoNotCollide() {
        val source = givenFile("photo.jpg")

        val first = assertNotNull(stagePickedFile(source))
        val second = assertNotNull(stagePickedFile(source))

        assertNotEquals(first.path, second.path)
        removeStaging(source)
        removeStaging(first)
        removeStaging(second)
    }

    @Test
    fun aMissingFileIsNotStaged() {
        val missing = NSURL.fileURLWithPath("${NSTemporaryDirectory().removeSuffix("/")}/calf-missing-pick.pdf")

        assertNull(stagePickedFile(missing))
    }

    private fun givenFile(name: String): NSURL = assertNotNull(stageExportData(NSData(), name))
}
