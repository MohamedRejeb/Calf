package com.mohamedrejeb.calf.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme
import kotlin.test.Test

/**
 * The plain Navigation 3 demo (a color palette) on the desktop: one pane pushes the color over
 * the list, a wide window shows the list beside the placeholder and then beside the open color.
 */
@OptIn(ExperimentalTestApi::class)
class Nav3ListDetailScreenTest {

    private class FakeWindow(private val width: Int) : WindowInfo {
        override val isWindowFocused: Boolean = true
        override val containerSize: IntSize get() = IntSize(width, WINDOW_HEIGHT)
    }

    @Test
    fun `narrow window pushes the item over the list and closes it again`() = runComposeUiTest {
        setContent { Demo(FakeWindow(NARROW)) }
        waitForIdle()
        onNodeWithText(PLACEHOLDER).assertDoesNotExist()

        onAllNodesWithText(TANGERINE).onFirst().performClick()
        waitForIdle()
        onNode(savedTaps(0)).performScrollTo().assertIsDisplayed()
        onNodeWithText(VERMILION).assertDoesNotExist()

        onNodeWithContentDescription("Close item").performClick()
        waitForIdle()
        onNodeWithText(VERMILION).assertIsDisplayed()
        onNode(savedTaps(0)).assertDoesNotExist()
    }

    @Test
    fun `wide window shows the placeholder, then the item beside the list`() = runComposeUiTest {
        setContent { Demo(FakeWindow(WIDE)) }
        waitForIdle()
        onNodeWithText(PLACEHOLDER).assertIsDisplayed()

        onAllNodesWithText(TANGERINE).onFirst().performClick()
        waitForIdle()
        onNodeWithText(PLACEHOLDER).assertDoesNotExist()
        onNodeWithText(VERMILION).assertIsDisplayed()
        // The row and the detail header.
        onAllNodesWithText(TANGERINE).assertCountEquals(2)

        // Another tap replaces the open item instead of stacking one more.
        onAllNodesWithText(MOSS).onFirst().performClick()
        waitForIdle()
        onAllNodesWithText(TANGERINE).assertCountEquals(1)
        onAllNodesWithText(MOSS).assertCountEquals(2)

        onNodeWithContentDescription("Close item").performClick()
        waitForIdle()
        onNodeWithText(PLACEHOLDER).assertIsDisplayed()
    }

    @Composable
    private fun Demo(window: FakeWindow) {
        CompositionLocalProvider(
            LocalWindowInfo provides window,
            LocalDensity provides Density(1f),
        ) {
            CalfTheme { Nav3ListDetailScreen(navigateBack = {}) }
        }
    }

    /** The state check's row: label and value are one node. */
    private fun savedTaps(count: Int): SemanticsMatcher = hasText("Saved taps") and hasText(count.toString())

    private companion object {
        // The first, third and fifth colors of the palette.
        const val VERMILION = "Vermilion"
        const val TANGERINE = "Tangerine"
        const val MOSS = "Moss"
        const val PLACEHOLDER = "No color selected"

        const val WIDE = 1200
        const val NARROW = 400
        const val WINDOW_HEIGHT = 800
    }
}
