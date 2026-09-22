package com.mohamedrejeb.calf.foldable.navigation3

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldAdaptStrategies
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.rememberCalfWindowAdaptiveInfo

/**
 * Material 3's list-detail scene strategy for Navigation 3, laid out around the fold on every
 * platform. A drop-in for `rememberListDetailSceneStrategy`:
 *
 * ```kotlin
 * NavDisplay(
 *     backStack = backStack,
 *     sceneStrategies = listOf(rememberCalfListDetailSceneStrategy()),
 *     entryProvider = entryProvider {
 *         entry<Inbox>(metadata = ListDetailSceneStrategy.listPane()) { InboxScreen() }
 *         entry<Message>(metadata = ListDetailSceneStrategy.detailPane()) { MessageScreen(it.id) }
 *     },
 * )
 * ```
 *
 * The only difference from upstream is the default [directive]: it is built from
 * [rememberCalfWindowAdaptiveInfo], which sees the fold on iOS too, by [calfPaneScaffoldDirective],
 * which gives two panes on an open book-style foldable. An open iPhone Duo shows the list beside
 * the detail; half open, the detail pane starts after the fold. Everything the scene does stays
 * upstream's: the pane animations, predictive back inside the scaffold, the placeholder, and
 * every other parameter with its upstream default.
 *
 * Nothing about the app's navigation changes. Shared element transitions, custom transitions and
 * predictive back keep working because the `NavDisplay` and its scenes are the ones from
 * Navigation 3.
 *
 * One deliberate difference from upstream's default directive, fold or no fold: the pane count is
 * capped at two, the rule calf-navigation's list-detail follows. An app that uses upstream's
 * `extraPane()` on a very wide window should pass its own [directive].
 */
@ExperimentalCalfFoldableApi
@ExperimentalMaterial3AdaptiveApi
@Composable
fun <T : Any> rememberCalfListDetailSceneStrategy(
    shouldHandleSinglePaneLayout: Boolean = false,
    backNavigationBehavior: BackNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
    directive: PaneScaffoldDirective = calfPaneScaffoldDirective(rememberCalfWindowAdaptiveInfo()),
    adaptStrategies: ThreePaneScaffoldAdaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
    paneExpansionDragHandle: (@Composable ThreePaneScaffoldScope.(PaneExpansionState) -> Unit)? = null,
    paneExpansionState: PaneExpansionState? = null,
): ListDetailSceneStrategy<T> =
    rememberListDetailSceneStrategy(
        shouldHandleSinglePaneLayout = shouldHandleSinglePaneLayout,
        backNavigationBehavior = backNavigationBehavior,
        directive = directive,
        adaptStrategies = adaptStrategies,
        paneExpansionDragHandle = paneExpansionDragHandle,
        paneExpansionState = paneExpansionState,
    )
