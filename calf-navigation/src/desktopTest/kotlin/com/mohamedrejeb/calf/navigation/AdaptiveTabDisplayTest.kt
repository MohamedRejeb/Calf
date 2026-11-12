package com.mohamedrejeb.calf.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

@OptIn(ExperimentalTestApi::class, ExperimentalCalfNavigationApi::class)
class AdaptiveTabDisplayTest {
    private data object Home : NavKey
    private data object Search : NavKey

    /** Pushed inside a tab; keeps the tab bar. */
    private data object Sub : NavKey

    /** Pushed inside a tab; hides the tab bar. */
    private data object Cover : NavKey

    private class ProbeViewModel : ViewModel()

    @Test
    fun `switching tabs keeps each tab's stack and saveable state`() = runComposeUiTest {
        lateinit var state: AdaptiveTabState<NavKey>
        setContent {
            state = rememberAdaptiveTabState<NavKey>(Home, Search)
            Tabs(state)
        }
        waitForIdle()

        onNodeWithText("home 0").performClick()
        onNodeWithText("home 1").assertIsDisplayed()

        state.backStack.add(Sub)
        waitForIdle()
        onNodeWithText("sub 0").performClick()
        onNodeWithText("sub 1").assertIsDisplayed()

        state.select(Search)
        waitForIdle()
        onNodeWithText("search content").assertIsDisplayed()

        state.select(Home)
        waitForIdle()
        onNodeWithText("sub 1").assertIsDisplayed()

        state.backStack.removeLastOrNull()
        waitForIdle()
        onNodeWithText("home 1").assertIsDisplayed()
    }

    @Test
    fun `tapping the selected tab pops it to the root`() = runComposeUiTest {
        lateinit var state: AdaptiveTabState<NavKey>
        setContent {
            state = rememberAdaptiveTabState<NavKey>(Home, Search)
            Tabs(state)
        }
        waitForIdle()

        state.backStack.add(Sub)
        waitForIdle()
        onNodeWithText("sub 0").assertIsDisplayed()

        onNodeWithText("Home tab").performClick()
        waitForIdle()

        onNodeWithText("home 0").assertIsDisplayed()
        assertEquals(listOf<NavKey>(Home), state.backStack.toList())
    }

    @Test
    fun `an entry with tabBarShown false hides the tab bar until it is popped`() = runComposeUiTest {
        lateinit var state: AdaptiveTabState<NavKey>
        setContent {
            state = rememberAdaptiveTabState<NavKey>(Home, Search)
            Tabs(state)
        }
        waitForIdle()
        onNodeWithText("Search tab").assertIsDisplayed()

        state.backStack.add(Cover)
        waitForIdle()
        onNodeWithText("cover content").assertIsDisplayed()
        onNodeWithText("Search tab").assertIsNotDisplayed()

        state.backStack.removeLastOrNull()
        waitForIdle()
        onNodeWithText("Search tab").assertIsDisplayed()
    }

    @Test
    fun `removing the selected tab falls back to the start tab`() = runComposeUiTest {
        var tabs by mutableStateOf(listOf<NavKey>(Home, Search))
        lateinit var state: AdaptiveTabState<NavKey>
        setContent {
            state = rememberAdaptiveTabState(*tabs.toTypedArray(), startTab = Home)
        }
        waitForIdle()
        state.select(Search)
        waitForIdle()

        tabs = listOf(Home)
        waitForIdle()

        assertEquals(Home, state.selectedTab)
    }

    @Test
    fun `two tab displays under one owner keep separate ViewModels`() = runComposeUiTest {
        val owner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        val models = arrayOfNulls<ViewModel>(2)
        setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                Column {
                    repeat(models.size) { index ->
                        Box(Modifier.weight(1f)) {
                            AdaptiveTabDisplay(
                                state = rememberAdaptiveTabState<NavKey>(Home),
                                tabs = listOf(adaptiveTab<NavKey>(Home, icon = {}, label = { Text("Home tab") })),
                                // Without Nav3's per-entry ViewModel decorator the entries
                                // resolve their ViewModels in the tab's own store.
                                entryDecorators = emptyList(),
                                materialLayoutType = NavigationSuiteType.NavigationBar,
                            ) { key ->
                                adaptiveNavEntry(key) { _ -> models[index] = viewModel { ProbeViewModel() } }
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()

        assertNotSame(assertNotNull(models[0]), assertNotNull(models[1]))
    }

    @Composable
    private fun Tabs(state: AdaptiveTabState<NavKey>) {
        val owner = remember {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }
        CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            AdaptiveTabDisplay(
                state = state,
                tabs = listOf(
                    adaptiveTab<NavKey>(Home, icon = {}, label = { Text("Home tab") }),
                    adaptiveTab<NavKey>(Search, icon = {}, label = { Text("Search tab") }),
                ),
                materialLayoutType = NavigationSuiteType.NavigationBar,
            ) { key ->
                when (key) {
                    is Home -> adaptiveNavEntry(key, title = { Text("Home") }) { padding ->
                        Body(padding) { Counter("home") }
                    }
                    is Search -> adaptiveNavEntry(key, title = { Text("Search") }) { padding ->
                        Body(padding) { Text("search content") }
                    }
                    is Sub -> adaptiveNavEntry(key, title = { Text("Sub") }) { padding ->
                        Body(padding) { Counter("sub") }
                    }
                    is Cover -> adaptiveNavEntry(key, barShown = false, tabBarShown = false) { padding ->
                        Body(padding) { Text("cover content") }
                    }
                    else -> error("Unexpected key $key")
                }
            }
        }
    }
}

/** Screen body honouring the navigator's bar insets, as real screens must. */
@Composable
private fun Body(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(padding)) { content() }
}

@Composable
private fun Counter(label: String) {
    var count by rememberSaveable { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("$label $count")
    }
}
