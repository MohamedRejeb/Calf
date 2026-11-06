package com.mohamedrejeb.calf.picker

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PartialPickFailureTest {
    @Test
    fun noFailureWhenEveryFileWasRead() {
        assertNull(partialPickFailure(failed = 0, picked = 3))
    }

    @Test
    fun theFailureSaysHowManyFilesWereLost() {
        assertEquals("1 of 3 picked files could not be read", partialPickFailure(failed = 1, picked = 3)?.message)
    }

    @Test
    fun aFailedPickIsReportedThenEndsWithAnEmptyResult() {
        val calls = mutableListOf<String>()
        val failure = FilePickerException("Could not show the file dialog")

        failPick(
            failure,
            onError = { calls += "onError: ${it.message}" },
            onResult = { calls += "onResult: ${it.size} files" },
        )

        assertEquals(listOf("onError: Could not show the file dialog", "onResult: 0 files"), calls)
    }
}
