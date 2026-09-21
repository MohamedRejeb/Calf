package com.mohamedrejeb.calf.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * The Material actual of a list-detail [AdaptiveNavDisplay], with the window width under the
 * test's control. One density-independent pixel is one pixel here.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCalfNavigationApi::class)
class AdaptiveListDetailDisplayTest {
    private data object Home : NavKey
    private data object Inbox : NavKey
    private data class Message(val id: Int) : NavKey
    private data object Attachment : NavKey

    private class ProbeViewModel : ViewModel()

    /** A window whose size the test sets; what `currentWindowAdaptiveInfo` reads. */
    private class FakeWindow(width: Int) : WindowInfo {
        var width by mutableIntStateOf(width)
        override val isWindowFocused: Boolean = true
        override val containerSize: IntSize get() = IntSize(width, WINDOW_HEIGHT)
    }

    private val viewModels = mutableMapOf<String, MutableList<ProbeViewModel>>()

    @Test
    fun `wide window shows the list and the detail side by side`() = runComposeUiTest {
        setContent { ListDetail(FakeWindow(WIDE), remember { NavBackStack<NavKey>(Inbox, Message(1)) }) }
        waitForIdle()

        onNodeWithText("inbox 0").assertIsDisplayed()
        onNodeWithText("message 1: 0").assertIsDisplayed()
    }

    @Test
    fun `wide window without a detail shows the placeholder of the list`() = runComposeUiTest {
        setContent { ListDetail(FakeWindow(WIDE), remember { NavBackStack<NavKey>(Inbox) }) }
        waitForIdle()

        onNodeWithText("inbox 0").assertIsDisplayed()
        onNodeWithText("select a message").assertIsDisplayed()
    }

    @Test
    fun `narrow window shows only the top entry`() = runComposeUiTest {
        setContent { ListDetail(FakeWindow(NARROW), remember { NavBackStack<NavKey>(Inbox, Message(1)) }) }
        waitForIdle()

        onNodeWithText("message 1: 0").assertIsDisplayed()
        onNodeWithText("inbox 0").assertDoesNotExist()
        onNodeWithText("select a message").assertDoesNotExist()
    }

    @Test
    fun `entry pushed after the detail takes the detail pane`() = runComposeUiTest {
        setContent { ListDetail(FakeWindow(WIDE), remember { NavBackStack<NavKey>(Inbox, Message(1), Attachment) }) }
        waitForIdle()

        onNodeWithText("inbox 0").assertIsDisplayed()
        onNodeWithText("attachment 0").assertIsDisplayed()
        onNodeWithText("message 1: 0").assertDoesNotExist()
    }

    @Test
    fun `entry below the list stays full screen in a wide window`() = runComposeUiTest {
        setContent { ListDetail(FakeWindow(WIDE), remember { NavBackStack<NavKey>(Home) }) }
        waitForIdle()

        onNodeWithText("home 0").assertIsDisplayed()
        onNodeWithText("select a message").assertDoesNotExist()
    }

    @Test
    fun `resizing the window keeps the back stack and every entry's state`() = runComposeUiTest {
        val window = FakeWindow(WIDE)
        lateinit var backStack: NavBackStack<NavKey>
        setContent {
            backStack = remember { NavBackStack<NavKey>(Inbox, Message(1), Attachment) }
            ListDetail(window, backStack)
        }
        waitForIdle()
        onNodeWithText("inbox 0").performClick()
        onNodeWithText("attachment 0").performClick()
        onNodeWithText("attachment 1").performClick()
        val stackBefore = backStack.toList()

        listOf(NARROW, WIDE, NARROW).forEach { width ->
            window.width = width
            waitForIdle()
            onNodeWithText("attachment 2").assertIsDisplayed()
            onNodeWithText("attachment remembered 2").assertIsDisplayed()
            assertEquals(stackBefore, backStack.toList(), "a layout change must never edit the back stack")
        }

        // Back to the list in one pane: its saved state was kept while it was covered.
        backStack.removeLastOrNull()
        backStack.removeLastOrNull()
        waitForIdle()
        onNodeWithText("inbox 1").assertIsDisplayed()

        // Each entry kept one ViewModel through every layout.
        assertEquals(1, viewModels.getValue("attachment").distinct().size)
        assertSame(viewModels.getValue("inbox").first(), viewModels.getValue("inbox").last())
    }

    @Test
    fun `selecting another item in two panes replaces the detail and what it pushed`() = runComposeUiTest {
        lateinit var backStack: NavBackStack<NavKey>
        setContent {
            backStack = remember { NavBackStack<NavKey>(Inbox, Message(1), Attachment) }
            ListDetail(FakeWindow(WIDE), backStack)
        }
        waitForIdle()
        onNodeWithText("inbox 0").performClick()

        backStack.selectDetail(list = Inbox, detail = Message(2))
        waitForIdle()

        assertEquals(listOf<NavKey>(Inbox, Message(2)), backStack.toList())
        onNodeWithText("message 2: 0").assertIsDisplayed()
        onNodeWithText("inbox 1").assertIsDisplayed()
    }

    @Composable
    private fun ListDetail(window: FakeWindow, backStack: NavBackStack<NavKey>) {
        CompositionLocalProvider(
            LocalWindowInfo provides window,
            LocalDensity provides Density(1f),
        ) {
            AdaptiveNavDisplay(
                backStack = backStack,
                modifier = Modifier.fillMaxSize(),
                listDetail = rememberAdaptiveListDetailSettings(),
            ) { key ->
                when (key) {
                    is Inbox -> adaptiveNavEntry(
                        key = key,
                        paneRole = AdaptivePaneRole.List { Text("select a message") },
                    ) { padding -> Counter("inbox", "inbox", Modifier.padding(padding)) }

                    is Message -> adaptiveNavEntry(key, paneRole = AdaptivePaneRole.Detail) { padding ->
                        Counter("message ${key.id}:", "message", Modifier.padding(padding))
                    }

                    is Attachment -> adaptiveNavEntry(key) { padding -> Counter("attachment", "attachment", Modifier.padding(padding)) }
                    else -> adaptiveNavEntry(key) { padding -> Counter("home", "home", Modifier.padding(padding)) }
                }
            }
        }
    }

    /**
     * Counts clicks twice. The button label uses saveable state, which also survives being covered
     * by another entry. The second text uses plain `remember`, the strictest check for an entry
     * that stays on screen: it only survives if the composition is moved between scenes rather
     * than restored. (Navigation 3 drops plain `remember` of a covered entry in any app.)
     */
    @Composable
    private fun Counter(label: String, probe: String, modifier: Modifier) {
        var saved by rememberSaveable { mutableStateOf(0) }
        var remembered by remember { mutableStateOf(0) }
        val viewModel = viewModel { ProbeViewModel() }
        viewModels.getOrPut(probe) { mutableListOf() }.add(viewModel)
        Column(modifier) {
            Button(onClick = { saved++; remembered++ }) { Text("$label $saved") }
            Text("$label remembered $remembered")
        }
    }

    private companion object {
        const val WIDE = 1200
        const val NARROW = 400
        const val WINDOW_HEIGHT = 800
    }
}
