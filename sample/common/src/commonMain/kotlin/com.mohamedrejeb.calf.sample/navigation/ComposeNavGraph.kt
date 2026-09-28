@file:OptIn(
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
    com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi::class,
    com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi::class,
)

package com.mohamedrejeb.calf.sample.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.calf.foldable.navigation3.rememberCalfListDetailSceneStrategy
import com.mohamedrejeb.calf.sample.screens.HomeDetailPlaceholder
import com.mohamedrejeb.calf.sample.screens.HomeScreen
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold

private val HomeKey = SampleScreen(Screen.Home)

/**
 * The sample on plain Compose navigation: one Navigation 3 back stack with Calf's fold-aware
 * list-detail strategy. Demos that live in the detail pane open beside the list when there is
 * room (open iPhone Duo, tablet, desktop), full-screen demos are pushed over everything.
 *
 * Every demo draws its own `AdaptiveScaffold` bars. On iOS the app's entry point is the bar host,
 * so those bars are the host containers' navigation bar, toolbar and tab bar: they stay in place
 * while Navigation 3 transitions, and on an iPhone Duo they sit in the vertical bar.
 */
@Composable
fun ComposeNavGraph(
    onNavigationChange: (SampleNavigation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(SampleSavedState, HomeKey)

    NavDisplay(
        backStack = backStack,
        modifier = modifier.fillMaxSize(),
        sceneStrategies = listOf(rememberCalfListDetailSceneStrategy()),
        entryProvider = { key ->
            val screen = (key as? SampleScreen)?.screen ?: error("Unexpected key: $key")
            composeEntry(key, screen, backStack, onNavigationChange)
        },
    )
}

private fun composeEntry(
    key: SampleScreen,
    screen: Screen,
    backStack: NavBackStack<NavKey>,
    onNavigationChange: (SampleNavigation) -> Unit,
): NavEntry<NavKey> =
    when (screen.host()) {
        SampleHost.List -> NavEntry(
            key = key,
            metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { HomeDetailPlaceholder() }),
        ) {
            // A scaffold without bars: on iOS the host hides them while the list is alone and leaves
            // them to a detail beside it. Its padding is the window's insets as far as they reach this pane.
            AdaptiveScaffold { padding ->
                HomeScreen(
                    contentPadding = padding,
                    selectedScreen = backStack.selectedDemo(),
                    navigation = SampleNavigation.Compose,
                    onNavigationChange = onNavigationChange,
                    navigate = { target -> backStack.open(target) },
                )
            }
        }

        SampleHost.Detail, SampleHost.DetailOwnBar -> NavEntry(
            key = key,
            metadata = ListDetailSceneStrategy.detailPane(),
        ) {
            DemoScreen(screen = screen, navigateBack = { backStack.removeLastOrNull() })
        }

        SampleHost.FullScreen -> NavEntry(key = key) {
            DemoScreen(screen = screen, navigateBack = { backStack.removeLastOrNull() })
        }
    }

/**
 * Opens [screen]. A detail-pane demo replaces the open one, so beside the list a tap swaps the
 * demo instead of stacking one per tap; a full-screen demo is pushed over whatever is open.
 */
private fun NavBackStack<NavKey>.open(screen: Screen) {
    if (screen.host().isDetail) retainAll { it == HomeKey }
    add(SampleScreen(screen))
}

/** The detail-pane demo open beside the list, or `null` while the placeholder shows. */
private fun NavBackStack<NavKey>.selectedDemo(): Screen? =
    filterIsInstance<SampleScreen>().lastOrNull { it.screen.host().isDetail }?.screen
