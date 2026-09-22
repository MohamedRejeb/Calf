package com.mohamedrejeb.calf.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme
import kotlin.test.Test

/**
 * Resizing the window across the two-pane threshold with an item open: the item stays open and
 * keeps its saved counter in both directions. The back stack is never edited by the layout.
 */
@OptIn(ExperimentalTestApi::class)
class Nav3ListDetailResizeTest {

    private class ResizableWindow(width: Int) : WindowInfo {
        var width by mutableIntStateOf(width)
        override val isWindowFocused: Boolean = true
        override val containerSize: IntSize get() = IntSize(width, WINDOW_HEIGHT)
    }

    @Test
    fun `an open item survives narrow to wide and back`() = runComposeUiTest {
        val window = ResizableWindow(NARROW)
        setContent { Demo(window) }
        waitForIdle()

        onAllNodesWithText("Item 3").onFirst().performClick()
        waitForIdle()
        onNodeWithText("Tap (0)").performClick()
        onNodeWithText("Tap (1)").performClick()
        waitForIdle()
        onNodeWithText("Saved taps: 2").assertIsDisplayed()
        onNodeWithText("Item 1").assertDoesNotExist()

        window.width = WIDE
        waitForIdle()
        // Two panes: the list is back beside the same item, counter intact.
        onNodeWithText("Item 1").assertIsDisplayed()
        onNodeWithText("Saved taps: 2").assertIsDisplayed()

        window.width = NARROW
        waitForIdle()
        // One pane again: the item is on top, still with its counter.
        onNodeWithText("Item 1").assertDoesNotExist()
        onNodeWithText("Saved taps: 2").assertIsDisplayed()
    }

    @Composable
    private fun Demo(window: ResizableWindow) {
        CompositionLocalProvider(
            LocalWindowInfo provides window,
            LocalDensity provides Density(1f),
        ) {
            CalfTheme { Nav3ListDetailScreen(navigateBack = {}) }
        }
    }

    private companion object {
        const val WIDE = 1200
        const val NARROW = 400
        const val WINDOW_HEIGHT = 800
    }
}
