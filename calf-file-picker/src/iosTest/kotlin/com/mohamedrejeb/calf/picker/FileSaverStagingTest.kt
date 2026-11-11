package com.mohamedrejeb.calf.picker

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The export picker shows, and saves under, the staged file's own name, so the staged file has to
 * carry the name the caller asked for. Uniqueness comes from the directory, not from the name.
 */
@OptIn(ExperimentalForeignApi::class)
class FileSaverStagingTest {
    @Test
    fun stagedFileKeepsTheRequestedName() {
        val url = assertNotNull(stageExportData(NSData(), "profile_picture.png"))
        assertEquals("profile_picture.png", url.lastPathComponent)
        removeStaging(url)
    }

    @Test
    fun twoStagingsOfTheSameNameDoNotCollide() {
        val first = assertNotNull(stageExportData(NSData(), "report.pdf"))
        val second = assertNotNull(stageExportData(NSData(), "report.pdf"))
        assertNotEquals(first.path, second.path)
        removeStaging(first)
        removeStaging(second)
    }

    @Test
    fun stagedFileLivesUnderTheTemporaryDirectoryAndIsWritten() {
        val bytes = byteArrayOf(1, 2, 3)
        val data = bytes.usePinnedData()
        val url = assertNotNull(stageExportData(data, "bytes.bin"))
        assertTrue(url.path!!.startsWith(NSTemporaryDirectory().removeSuffix("/")))
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(url.path!!))
        removeStaging(url)
        assertTrue(!NSFileManager.defaultManager.fileExistsAtPath(url.path!!))
    }

    @Test
    fun stagingAnExistingFileCopiesItUnderTheRequestedName() {
        val source = assertNotNull(stageExportData(byteArrayOf(9).usePinnedData(), "source.tmp"))
        val staged = assertNotNull(stageExportFile(source, "photo.png"))
        assertEquals("photo.png", staged.lastPathComponent)
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(staged.path!!))
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(source.path!!), "the source is left in place")
        removeStaging(source)
        removeStaging(staged)
    }

    @Test
    fun stagingAMissingFileFails() {
        val missing = NSURL.fileURLWithPath("${NSTemporaryDirectory().removeSuffix("/")}/calf-missing-source.pdf")
        assertNull(stageExportFile(missing, "photo.png"))
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.usePinnedData(): NSData =
    usePinned { pinned -> NSData.dataWithBytes(pinned.addressOf(0), size.toULong()) }
