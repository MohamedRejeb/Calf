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

/**
 * Bookmarks on a simulator resolve inside the app container. The sandbox rule they exist for,
 * that a picked location outside the container stays readable after a restart, needs a device.
 */
@OptIn(ExperimentalForeignApi::class)
class KmpFileBookmarkTest {
    private val context = PlatformContext.INSTANCE
    private val directory = "${NSTemporaryDirectory().removeSuffix("/")}/calf-io-bookmark-${NSUUID().UUIDString}"

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
    fun aBookmarkReopensTheSameFile() {
        val url = givenFile("Report.pdf", size = 16u)
        val bookmark = assertNotNull(KmpFile(url).createBookmark(context))

        val restored = assertNotNull(KmpFile.fromBookmark(bookmark, context))

        assertEquals(url.URLByResolvingSymlinksInPath?.path, restored.url.URLByResolvingSymlinksInPath?.path)
        assertEquals(16L, restored.getSize(context))
        restored.releaseBookmark(context)
    }

    @Test
    fun releasingTwiceIsHarmless() {
        val bookmark = assertNotNull(KmpFile(givenFile("a.bin", size = 1u)).createBookmark(context))
        val restored = assertNotNull(KmpFile.fromBookmark(bookmark, context))

        restored.releaseBookmark(context)
        restored.releaseBookmark(context)
    }

    @Test
    fun aBookmarkOfAMissingFileIsNotCreated() {
        assertNull(KmpFile(NSURL.fileURLWithPath("$directory/missing.pdf")).createBookmark(context))
    }

    @Test
    fun somethingThatIsNotABookmarkDoesNotReopen() {
        assertNull(KmpFile.fromBookmark("not a bookmark", context))
        assertNull(KmpFile.fromBookmark("", context))
    }

    private fun givenFile(name: String, size: ULong): NSURL {
        val url = NSURL.fileURLWithPath("$directory/$name")
        NSFileManager.defaultManager.createFileAtPath(
            path = assertNotNull(url.path),
            contents = NSMutableData.dataWithLength(size),
            attributes = null,
        )
        return url
    }
}
