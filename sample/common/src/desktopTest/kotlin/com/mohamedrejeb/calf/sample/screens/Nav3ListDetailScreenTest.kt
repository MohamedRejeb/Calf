package com.mohamedrejeb.calf.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme
import kotlin.test.Test

/**
 * The plain Navigation 3 demo on the desktop: one pane pushes the item over the list, a wide
 * window shows the list beside the placeholder and then beside the open item.
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
        onNodeWithText("Select an item").assertDoesNotExist()

        onAllNodesWithText("Item 3").onFirst().performClick()
        waitForIdle()
        onNodeWithText("Saved taps: 0").assertIsDisplayed()
        onNodeWithText("Item 1").assertDoesNotExist()

        onNodeWithContentDescription("Close item").performClick()
        waitForIdle()
        onNodeWithText("Item 1").assertIsDisplayed()
        onNodeWithText("Saved taps: 0").assertDoesNotExist()
    }

    @Test
    fun `wide window shows the placeholder, then the item beside the list`() = runComposeUiTest {
        setContent { Demo(FakeWindow(WIDE)) }
        waitForIdle()
        onNodeWithText("Select an item").assertIsDisplayed()

        onAllNodesWithText("Item 3").onFirst().performClick()
        waitForIdle()
        onNodeWithText("Select an item").assertDoesNotExist()
        onNodeWithText("Item 1").assertIsDisplayed()
        // The row and the detail header.
        onAllNodesWithText("Item 3").assertCountEquals(2)

        // Another tap replaces the open item instead of stacking one more.
        onAllNodesWithText("Item 5").onFirst().performClick()
        waitForIdle()
        onAllNodesWithText("Item 3").assertCountEquals(1)
        onAllNodesWithText("Item 5").assertCountEquals(2)

        onNodeWithContentDescription("Close item").performClick()
        waitForIdle()
        onNodeWithText("Select an item").assertIsDisplayed()
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

    private companion object {
        const val WIDE = 1200
        const val NARROW = 400
        const val WINDOW_HEIGHT = 800
    }
}
