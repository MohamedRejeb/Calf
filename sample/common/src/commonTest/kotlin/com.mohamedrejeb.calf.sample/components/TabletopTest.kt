package com.mohamedrejeb.calf.sample.components

import androidx.compose.ui.geometry.Rect
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldOrientation
import com.mohamedrejeb.calf.foldable.FoldPanes
import com.mohamedrejeb.calf.foldable.FoldPosture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCalfFoldableApi::class)
class TabletopTest {
    private val across = FoldPanes(
        first = Rect(0f, 0f, 800f, 380f),
        second = Rect(0f, 420f, 800f, 800f),
        gap = Rect(0f, 380f, 800f, 420f),
        orientation = FoldOrientation.Horizontal,
    )
    private val book = FoldPanes(
        first = Rect(0f, 0f, 380f, 800f),
        second = Rect(420f, 0f, 800f, 800f),
        gap = Rect(380f, 0f, 420f, 800f),
        orientation = FoldOrientation.Vertical,
    )

    @Test
    fun halfOpenWithTheFoldRunningAcrossIsTabletop() {
        assertEquals(across, tabletopPanes(FoldPosture.HalfOpen, across))
    }

    @Test
    fun aBookPostureIsNotTabletop() {
        assertNull(tabletopPanes(FoldPosture.HalfOpen, book))
    }

    @Test
    fun aFlatDeviceIsNotTabletopEvenWithAHorizontalFold() {
        assertNull(tabletopPanes(FoldPosture.Flat, across))
    }

    @Test
    fun onePaneIsNeverTabletop() {
        assertNull(tabletopPanes(FoldPosture.HalfOpen, FoldPanes(first = Rect(0f, 0f, 800f, 800f))))
        assertNull(tabletopPanes(FoldPosture.NoFold, FoldPanes(first = Rect(0f, 0f, 800f, 800f))))
    }
}
