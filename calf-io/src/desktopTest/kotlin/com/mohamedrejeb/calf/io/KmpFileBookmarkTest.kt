package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Desktop apps can reopen any path they could open before, so a bookmark is the path. */
class KmpFileBookmarkTest {
    private val context = PlatformContext.INSTANCE
    private val directory = createTempDirectory("calf-io-bookmark").toFile()

    @AfterTest
    fun deleteDirectory() {
        directory.deleteRecursively()
    }

    @Test
    fun aBookmarkReopensTheSameFolder() {
        val bookmark = assertNotNull(KmpFile(directory).createBookmark(context))

        val restored = assertNotNull(KmpFile.fromBookmark(bookmark, context))

        assertEquals(directory.absolutePath, restored.file.absolutePath)
    }

    @Test
    fun aBookmarkOfAFileThatIsGoneDoesNotReopen() {
        val file = File(directory, "Report.pdf").apply { writeText("report") }
        val bookmark = assertNotNull(KmpFile(file).createBookmark(context))
        file.delete()

        assertNull(KmpFile.fromBookmark(bookmark, context))
    }

    @Test
    fun releasingABookmarkKeepsTheFile() {
        val file = KmpFile(directory)

        file.releaseBookmark(context)

        assertEquals(true, file.exists(context))
    }
}
