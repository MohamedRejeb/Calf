@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalCalfNavigationApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import com.mohamedrejeb.calf.navigation.AdaptivePaneRole
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.navigation.kind

/**
 * What a Material `NavDisplay` needs to lay a back stack out as list-detail.
 *
 * @property sceneStrategies Material 3's list-detail scene, first in line. Navigation 3 falls back
 *   to its single-pane scene when that one declines: one pane, or a top entry without a side.
 * @property entryProvider the caller's provider, with every entry's Calf role translated into the
 *   metadata Material's scene reads.
 */
internal class MaterialListDetail(
    val sceneStrategies: List<SceneStrategy<NavKey>>,
    val entryProvider: (NavKey) -> NavEntry<NavKey>,
)

/**
 * Wraps Material 3's `ListDetailSceneStrategy` rather than re-implementing its scene: the scene
 * owns the pane animations, predictive back inside the scaffold and the hinge-aware pane bounds.
 *
 * Back always pops exactly one entry (`PopLatest`), in one pane and in two. The back stack is the
 * single source of truth on every platform, and iOS pops one controller at a time as well.
 *
 * Material's roles are per entry, Calf's detail side is positional (everything pushed after the
 * detail stays in the detail pane), so the sides are worked out for the whole [backStack] and
 * looked up by key. Reading [backStack] here is what recomputes them when it changes.
 */
@Composable
internal fun materialListDetail(
    backStack: List<NavKey>,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): MaterialListDetail {
    val strategy = rememberListDetailSceneStrategy<NavKey>(
        backNavigationBehavior = BackNavigationBehavior.PopLatest,
        directive = listDetailDirective(),
    )

    val keys = backStack.toList()
    // Built once per recomposition and handed to NavDisplay below, instead of being built twice.
    val entries = keys.associateWith(entryProvider)
    val roles = entries.mapValues { (_, entry) -> entry.adaptiveMetadata.paneRole }
    val roleKindOf = { key: NavKey -> roles.getValue(key).kind }
    val sides = paneSidesOf(keys, roleKindOf)
    val listRole = partitionListDetail(keys, roleKindOf).list?.let(roles::get) as? AdaptivePaneRole.List
    val placeholder = listRole?.detailPlaceholder ?: {}

    return MaterialListDetail(
        sceneStrategies = listOf(strategy),
        entryProvider = { key ->
            val entry = entries[key] ?: entryProvider(key)
            when (sides[key]) {
                PaneSide.List -> entry.withMetadata(key, ListDetailSceneStrategy.listPane(detailPlaceholder = { placeholder() }))
                PaneSide.Detail -> entry.withMetadata(key, ListDetailSceneStrategy.detailPane())
                null -> entry
            }
        },
    )
}

/**
 * Material's own directive for the current window, with the pane count from
 * [materialHorizontalPartitions]. Hinge handling stays Material's: a hinge that separates the
 * display (half open) is kept clear, with the panes on either side of it.
 */
@Composable
private fun listDetailDirective(): PaneScaffoldDirective {
    val windowInfo = currentWindowAdaptiveInfoV2()
    val upstream = calculatePaneScaffoldDirective(windowInfo)
    val windowWidthDp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }

    val partitions = materialHorizontalPartitions(
        upstreamPartitions = upstream.maxHorizontalPartitions,
        windowWidthDp = windowWidthDp.value,
        hasVerticalHinge = windowInfo.windowPosture.hingeList.any { it.isVertical },
    )
    if (partitions == upstream.maxHorizontalPartitions) return upstream

    return upstream.copy(
        maxHorizontalPartitions = partitions,
        horizontalPartitionSpacerSize = if (partitions > 1) PANE_SPACER else 0.dp,
    )
}

/** A copy of this entry with [extra] metadata. `NavEntry` keeps its key private, hence [key]. */
private fun NavEntry<NavKey>.withMetadata(key: NavKey, extra: Map<String, Any>): NavEntry<NavKey> =
    NavEntry(key = key, contentKey = contentKey, metadata = metadata + extra) { Content() }

/** Material's spacer between two horizontal partitions. */
private val PANE_SPACER = 24.dp
