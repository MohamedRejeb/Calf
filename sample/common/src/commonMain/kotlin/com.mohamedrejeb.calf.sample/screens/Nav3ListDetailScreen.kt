@file:OptIn(
    androidx.compose.animation.ExperimentalSharedTransitionApi::class,
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
    com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi::class,
)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.mohamedrejeb.calf.foldable.navigation3.rememberCalfListDetailSceneStrategy
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.navigation.Screen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

// A list and its detail on one Navigation 3 back stack, without calf-navigation.
@Serializable
internal data object Nav3Inbox : NavKey

@Serializable
internal data class Nav3Item(val id: Int) : NavKey

/** Nav3's own back stack needs the keys registered to survive process death and Android folds. */
private val Nav3SavedState: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Nav3Inbox::class)
            subclass(Nav3Item::class)
        }
    }
}

/**
 * Plain Navigation 3 with Calf's fold-aware list-detail strategy: `NavDisplay`, upstream's
 * `listPane()` / `detailPane()` metadata and a shared element transition between a row and the
 * detail header, which calf-navigation's UIKit containers cannot do. The one line that comes from
 * Calf is `rememberCalfListDetailSceneStrategy()`: it gives two panes on an open iPhone Duo and
 * lays the detail out after the fold when the Duo is half open.
 */
@Composable
fun Nav3ListDetailScreen(navigateBack: () -> Unit) {
    SampleScreenScaffold(title = Screen.Nav3ListDetail.title, navigateBack = navigateBack) { padding ->
        Nav3ListDetail(modifier = Modifier.fillMaxSize().padding(padding))
    }
}

@Composable
private fun Nav3ListDetail(modifier: Modifier) {
    val backStack = rememberNavBackStack(Nav3SavedState, Nav3Inbox)

    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(LocalNav3SharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                sharedTransitionScope = this,
                sceneStrategies = listOf(rememberCalfListDetailSceneStrategy()),
                entryProvider = entryProvider {
                    entry<Nav3Inbox>(
                        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { Nav3NoItemSelected() }),
                    ) {
                        Nav3ItemList(
                            // Read in the composition, so the highlight follows the back stack.
                            selectedId = backStack.filterIsInstance<Nav3Item>().lastOrNull()?.id,
                            onOpen = backStack::openItem,
                        )
                    }
                    entry<Nav3Item>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                        Nav3ItemDetail(id = key.id, onClose = { backStack.removeLastOrNull() })
                    }
                },
            )
        }
    }
}

/**
 * Shows [id] as the one open item. In two panes the list stays where it is, and a tap replaces
 * the open item instead of stacking one per tap; in one pane it is a push.
 */
private fun NavBackStack<NavKey>.openItem(id: Int) {
    retainAll { it is Nav3Inbox }
    add(Nav3Item(id))
}

@Composable
private fun Nav3ItemList(selectedId: Int?, onOpen: (Int) -> Unit) {
    LazyColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxSize()) {
        items(count = ITEM_COUNT, key = { index -> index }) { index ->
            val id = index + 1
            ListItem(
                leadingContent = { Nav3Swatch(id = id, modifier = Modifier.size(40.dp)) },
                headlineContent = { Text("Item $id", modifier = Modifier.nav3SharedBounds("title-$id")) },
                supportingContent = { Text("Tap to open. The swatch and the title travel to the detail.") },
                tonalElevation = if (id == selectedId) 4.dp else 0.dp,
                modifier = Modifier.fillMaxWidth().clickable { onOpen(id) },
            )
            HorizontalDivider()
        }
    }
}

/** What has to survive a fold: the saved counter and the scroll position. */
@Composable
private fun Nav3ItemDetail(id: Int, onClose: () -> Unit) {
    var taps by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        state = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        item { Nav3DetailHeader(id = id, onClose = onClose) }
        item { Text("Saved taps: $taps") }
        item { Button(onClick = { taps += 1 }) { Text("Tap ($taps)") } }
        items(DETAIL_LINE_COUNT) { index ->
            Text("Line ${index + 1} · scroll, then open or close the fold, rotate, or resize the window")
        }
    }
}

@Composable
private fun Nav3DetailHeader(id: Int, onClose: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close item")
        }
        Nav3Swatch(id = id, modifier = Modifier.size(72.dp))
        Text(
            text = "Item $id",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.nav3SharedBounds("title-$id"),
        )
    }
}

@Composable
private fun Nav3Swatch(id: Int, modifier: Modifier) {
    Box(
        modifier = modifier
            .nav3SharedElement("swatch-$id")
            .background(swatchColor(id), MaterialTheme.shapes.medium),
    )
}

@Composable
private fun Nav3NoItemSelected() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select an item", style = MaterialTheme.typography.titleMedium)
    }
}

private val LocalNav3SharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    staticCompositionLocalOf { error("Only inside the Navigation 3 list-detail demo") }

/**
 * The shared element for the one-pane push and pop. In two panes the row and the detail header
 * are on screen at the same time, and two visible matches of one key are not a transition, so the
 * modifier is left out there. [LocalListDetailSceneScope] is only provided inside the two-pane scene.
 */
@Composable
private fun Modifier.nav3SharedElement(key: String): Modifier {
    if (LocalListDetailSceneScope.current != null) return this
    return with(LocalNav3SharedTransitionScope.current) {
        this@nav3SharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
        )
    }
}

/** Like [nav3SharedElement], for content whose size differs between the row and the header. */
@Composable
private fun Modifier.nav3SharedBounds(key: String): Modifier {
    if (LocalListDetailSceneScope.current != null) return this
    return with(LocalNav3SharedTransitionScope.current) {
        this@nav3SharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
        )
    }
}

private fun swatchColor(id: Int): Color =
    Color.hsl(hue = (id * HUE_STEP_DEGREES % FULL_CIRCLE_DEGREES).toFloat(), saturation = 0.55f, lightness = 0.55f)

private const val ITEM_COUNT = 30
private const val DETAIL_LINE_COUNT = 60
private const val HUE_STEP_DEGREES = 37
private const val FULL_CIRCLE_DEGREES = 360
