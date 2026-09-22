package com.mohamedrejeb.calf.foldable.navigation3

import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The directive fed to Material 3's list-detail scene. Upstream decides the pane count from the
 * window width alone (two from 840dp); Calf adds the open book-style foldable, whose inner
 * display is often narrower, and caps the count at two. Everything else stays upstream's.
 */
class CalfPaneScaffoldDirectiveTest {

    private val compact = WindowSizeClass(minWidthDp = 0, minHeightDp = 0)
    private val medium = WindowSizeClass(minWidthDp = 600, minHeightDp = 480)
    private val expanded = WindowSizeClass(minWidthDp = 840, minHeightDp = 900)
    private val large = WindowSizeClass(minWidthDp = 1200, minHeightDp = 900)

    private val duoBounds = Rect(475.5f, 0f, 475.5f, 669f)

    private fun verticalHinge(isSeparating: Boolean) = HingeInfo(
        bounds = duoBounds,
        isFlat = !isSeparating,
        isVertical = true,
        isSeparating = isSeparating,
        isOccluding = false,
    )

    private val horizontalHalfOpen = HingeInfo(
        bounds = Rect(0f, 330f, 669f, 330f),
        isFlat = false,
        isVertical = false,
        isSeparating = true,
        isOccluding = false,
    )

    @Test
    fun `without a fold the directive is upstream's own`() {
        for (sizeClass in listOf(compact, medium, expanded)) {
            val info = WindowAdaptiveInfo(sizeClass, Posture())

            assertEquals(calculatePaneScaffoldDirective(info), calfPaneScaffoldDirective(info), "minWidth ${sizeClass.minWidthDp}")
        }
    }

    @Test
    fun `open duo at medium width gets two panes with material's spacer`() {
        val info = WindowAdaptiveInfo(medium, Posture(hingeList = listOf(verticalHinge(isSeparating = false))))
        val upstream = calculatePaneScaffoldDirective(info)

        val directive = calfPaneScaffoldDirective(info)

        assertEquals(1, upstream.maxHorizontalPartitions)
        assertEquals(2, directive.maxHorizontalPartitions)
        assertEquals(24.dp, directive.horizontalPartitionSpacerSize)
        assertEquals(upstream.maxVerticalPartitions, directive.maxVerticalPartitions)
        assertEquals(upstream.verticalPartitionSpacerSize, directive.verticalPartitionSpacerSize)
        assertEquals(upstream.defaultPanePreferredWidth, directive.defaultPanePreferredWidth)
        assertEquals(upstream.defaultPanePreferredHeight, directive.defaultPanePreferredHeight)
        assertEquals(emptyList(), directive.excludedBounds)
    }

    @Test
    fun `half open duo keeps the separating fold clear`() {
        val info = WindowAdaptiveInfo(medium, Posture(hingeList = listOf(verticalHinge(isSeparating = true))))

        val directive = calfPaneScaffoldDirective(info)

        assertEquals(2, directive.maxHorizontalPartitions)
        assertEquals(listOf(duoBounds), directive.excludedBounds)
    }

    @Test
    fun `hinge policy is passed on to upstream`() {
        val info = WindowAdaptiveInfo(medium, Posture(hingeList = listOf(verticalHinge(isSeparating = false))))

        val directive = calfPaneScaffoldDirective(info, verticalHingePolicy = HingePolicy.AlwaysAvoid)

        assertEquals(listOf(duoBounds), directive.excludedBounds)
    }

    @Test
    fun `compact width stays one pane even with a vertical hinge`() {
        val info = WindowAdaptiveInfo(compact, Posture(hingeList = listOf(verticalHinge(isSeparating = true))))

        assertEquals(calculatePaneScaffoldDirective(info), calfPaneScaffoldDirective(info))
    }

    @Test
    fun `tabletop is upstream's own`() {
        val info = WindowAdaptiveInfo(medium, Posture(isTabletop = true, hingeList = listOf(horizontalHalfOpen)))

        val directive = calfPaneScaffoldDirective(info)

        assertEquals(calculatePaneScaffoldDirective(info), directive)
        assertEquals(1, directive.maxHorizontalPartitions)
        assertEquals(2, directive.maxVerticalPartitions)
    }

    @Test
    fun `expanded width already has two panes upstream`() {
        val info = WindowAdaptiveInfo(expanded, Posture(hingeList = listOf(verticalHinge(isSeparating = false))))

        assertEquals(calculatePaneScaffoldDirective(info), calfPaneScaffoldDirective(info))
    }

    @Test
    fun `large width is capped at two panes, there is no third one`() {
        val info = WindowAdaptiveInfo(large, Posture())
        val upstream = calculatePaneScaffoldDirective(info)

        val directive = calfPaneScaffoldDirective(info)

        assertEquals(3, upstream.maxHorizontalPartitions)
        assertEquals(2, directive.maxHorizontalPartitions)
        assertEquals(upstream.horizontalPartitionSpacerSize, directive.horizontalPartitionSpacerSize)
        assertEquals(upstream.defaultPanePreferredWidth, directive.defaultPanePreferredWidth)
    }

    @Test
    fun `equal inputs give equal directives`() {
        val info = WindowAdaptiveInfo(medium, Posture(hingeList = listOf(verticalHinge(isSeparating = true))))
        val again = WindowAdaptiveInfo(medium, Posture(hingeList = listOf(verticalHinge(isSeparating = true))))

        assertEquals(calfPaneScaffoldDirective(info), calfPaneScaffoldDirective(again))
    }
}
