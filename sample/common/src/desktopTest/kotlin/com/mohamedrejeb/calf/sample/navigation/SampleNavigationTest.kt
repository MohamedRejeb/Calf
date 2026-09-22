package com.mohamedrejeb.calf.sample.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.hasText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme
import kotlin.test.Test

/**
 * Walks the sample's navigation on the Material actual: every [Screen] opens from the home list
 * and comes back, in one pane and in two.
 */
@OptIn(ExperimentalTestApi::class)
class SampleNavigationTest {

    /** A window whose width the test sets; what the list-detail scene reads. */
    private class FakeWindow(width: Int) : WindowInfo {
        var width by mutableIntStateOf(width)
        override val isWindowFocused: Boolean = true
        override val containerSize: IntSize get() = IntSize(width, WINDOW_HEIGHT)
    }

    @Test
    fun `every demo opens from the list and navigates back in one pane`() = runComposeUiTest {
        setContent { Sample(FakeWindow(NARROW)) }
        waitForIdle()

        // The desktop web view is a Swing panel, which the headless test host cannot show.
        Screen.entries.filter { it != Screen.Home && it != Screen.WebView }.forEach { screen ->
            openFromList(screen)
            onNodeWithText("Calf").assertDoesNotExist()
            onAllNodesWithText(expectedTextOf(screen)).onFirst().assertExists("$screen did not open")

            goBack(screen)
            onNodeWithText("Calf").assertExists("the list is not back after $screen")
        }
    }

    @Test
    fun `wide window shows the placeholder, then the selected demo beside the list`() = runComposeUiTest {
        setContent { Sample(FakeWindow(WIDE)) }
        waitForIdle()
        onNodeWithText("Select a component").assertIsDisplayed()

        openFromList(Screen.Switch)
        onNodeWithText("Select a component").assertDoesNotExist()
        onNodeWithText("Calf").assertIsDisplayed()
        // The list item and the detail's bar title.
        onAllNodesWithText(Screen.Switch.title).assertCountEquals(2)

        // Selecting another demo replaces the detail: one "Adaptive Switch" (the list item) is left.
        openFromList(Screen.Slider)
        onAllNodesWithText(Screen.Switch.title).assertCountEquals(1)
        onAllNodesWithText(Screen.Slider.title).assertCountEquals(2)

        // Back from the last detail leaves the placeholder.
        goBack(Screen.Slider)
        onNodeWithText("Select a component").assertIsDisplayed()
    }

    @Test
    fun `a full-screen demo covers the list and the open detail, and both are back after it`() = runComposeUiTest {
        setContent { Sample(FakeWindow(WIDE)) }
        waitForIdle()
        openFromList(Screen.Switch)

        openFromList(Screen.AdaptiveNavigation)
        onNodeWithText("Calf").assertDoesNotExist()
        onNodeWithText("One stack per tab").assertIsDisplayed()

        goBack(Screen.AdaptiveNavigation)
        onNodeWithText("Calf").assertIsDisplayed()
        onNodeWithText("Select a component").assertDoesNotExist()
        // The list kept its scroll position while covered; bring the item back into view.
        scrollListTo(Screen.Switch)
        onAllNodesWithText(Screen.Switch.title).assertCountEquals(2)
    }

    private fun ComposeUiTest.openFromList(screen: Screen) {
        scrollListTo(screen)
        onAllNodesWithText(screen.title).onFirst().performClick()
        waitForIdle()
    }

    private fun ComposeUiTest.scrollListTo(screen: Screen) {
        onAllNodes(hasScrollToNodeAction()).onFirst().performScrollToNode(hasText(screen.title))
        waitForIdle()
    }

    /** Presses the bar's back button: the topmost one, since a demo's body can have its own "Back". */
    private fun ComposeUiTest.goBack(screen: Screen) {
        val label = if (screen == Screen.AdaptiveNavigation) "Exit demo" else "Back"
        val buttons = onAllNodesWithContentDescription(label)
        val topmost = buttons.fetchSemanticsNodes().withIndex().minBy { (_, node) -> node.boundsInRoot.top }.index
        buttons[topmost].performClick()
        waitForIdle()
    }

    @Composable
    private fun Sample(window: FakeWindow) {
        CompositionLocalProvider(
            LocalWindowInfo provides window,
            LocalDensity provides Density(1f),
        ) {
            CalfTheme { AppNavGraph() }
        }
    }

    /** What a demo shows once open. */
    private fun expectedTextOf(screen: Screen): String =
        when (screen) {
            Screen.AdaptiveNavigation -> "One stack per tab"
            Screen.NavigationBar, Screen.ScaffoldDemo -> "Home"
            else -> screen.title
        }

    private companion object {
        const val WIDE = 1200
        const val NARROW = 400
        const val WINDOW_HEIGHT = 800
    }
}
