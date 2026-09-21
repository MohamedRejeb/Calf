package com.mohamedrejeb.calf.foldable

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@OptIn(CalfIosOnlyApi::class)
class FoldStateTest {

    private val window = Size(951f, 669f)
    private val division = FoldRegion(
        bounds = Rect(455.5f, 0f, 495.5f, 669f),
        margins = FoldMargins(left = 20f, right = 20f),
    )

    private fun duoSnapshot(posture: FoldPosture) = FoldSnapshot(
        posture = posture,
        fold = foldInfoOfIosRegion(division, posture),
        iosFoldRegion = division,
    )

    @Test
    fun `fake state exposes its snapshot`() {
        val snapshot = duoSnapshot(FoldPosture.HalfOpen)

        val state = FoldState(snapshot)

        assertEquals(snapshot, state.snapshot)
        assertEquals(FoldPosture.HalfOpen, state.posture)
        assertEquals(snapshot.fold, state.fold)
        assertEquals(true, state.isSeparating)
        assertEquals(division, state.iosFoldRegion)
        assertEquals(snapshot.toWindowPosture(), state.toWindowPosture())
    }

    @Test
    fun `default fake has no fold and a silent angle`() = runTest {
        val state = FoldState()

        assertEquals(FoldSnapshot.NoFold, state.snapshot)
        assertEquals(1, state.calculatePanes(window).count)
        assertEquals(emptyList(), state.iosHingeAngle.toList())
    }

    @Test
    fun `fake angle flow is handed through`() = runTest {
        val state = FoldState(iosHingeAngle = flowOf(90f, 180f))

        assertEquals(listOf(90f, 180f), state.iosHingeAngle.toList())
    }

    @Test
    fun `panes keep the iOS margins clear while half open`() {
        val panes = FoldState(duoSnapshot(FoldPosture.HalfOpen)).calculatePanes(window)

        assertEquals(Rect(0f, 0f, 455.5f, 669f), panes.first)
        assertEquals(Rect(495.5f, 0f, 951f, 669f), panes.second)
    }

    @Test
    fun `panes touch at the crease while flat`() {
        val panes = FoldState(duoSnapshot(FoldPosture.Flat)).calculatePanes(window)

        assertEquals(Rect(0f, 0f, 475.5f, 669f), panes.first)
        assertEquals(Rect(475.5f, 0f, 951f, 669f), panes.second)
    }

    @Test
    fun `minimum pane size is passed on`() {
        val state = FoldState(duoSnapshot(FoldPosture.Flat))

        assertEquals(1, state.calculatePanes(window, minPaneSize = 500f).count)
    }

    @Test
    fun `debug dump of a fake names the snapshot`() {
        val dump = FoldState(duoSnapshot(FoldPosture.Flat)).iosDebugDump()

        assertContains(dump, "posture=Flat")
    }
}
