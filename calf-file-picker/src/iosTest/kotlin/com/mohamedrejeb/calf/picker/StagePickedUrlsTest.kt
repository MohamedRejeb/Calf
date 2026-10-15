package com.mohamedrejeb.calf.picker

import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** A picked file that cannot be copied is reported, and the others are still delivered. */
class StagePickedUrlsTest {
    @Test
    fun everyReadableFileIsDeliveredWithoutAFailure() {
        val source = assertNotNull(stageExportData(NSData(), "a.pdf"))

        val staged = stagePickedUrls(listOf(source))

        assertEquals(listOf("a.pdf"), staged.files.map { it.url.lastPathComponent })
        assertNull(staged.failure)
        staged.files.forEach { removeStaging(it.url) }
        removeStaging(source)
    }

    @Test
    fun anUnreadableFileIsReportedAndTheOthersAreKept() {
        val source = assertNotNull(stageExportData(NSData(), "a.pdf"))
        val missing = NSURL.fileURLWithPath("${NSTemporaryDirectory().removeSuffix("/")}/calf-missing-pick.pdf")

        val staged = stagePickedUrls(listOf(source, missing))

        assertEquals(listOf("a.pdf"), staged.files.map { it.url.lastPathComponent })
        assertEquals("1 of 2 picked files could not be read", staged.failure?.message)
        staged.files.forEach { removeStaging(it.url) }
        removeStaging(source)
    }
}
