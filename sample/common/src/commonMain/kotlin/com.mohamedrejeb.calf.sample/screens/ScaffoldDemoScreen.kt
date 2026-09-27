package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

private const val SCAFFOLD_TAB_FADE_MILLIS = 300

/** The tabs of the scaffold demo, with their Material icons and their UITabBar item. */
private enum class ScaffoldTab(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val iosItem: UIKitUITabBarItem,
) {
    Home(
        title = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        iosItem = UIKitUITabBarItem(title = "Home", image = UIKitImage.SystemName(SFSymbol.houseFill)),
    ),
    Favorites(
        title = "Favorites",
        icon = Icons.Outlined.FavoriteBorder,
        selectedIcon = Icons.Filled.Favorite,
        iosItem = UIKitUITabBarItem(title = "Favorites", image = UIKitImage.Vector(Icons.Rounded.Favorite)),
    ),
    Settings(
        title = "Settings",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        iosItem = UIKitUITabBarItem(title = "Settings", image = UIKitImage.Vector(Icons.Rounded.Settings)),
    ),
}

private val ScaffoldTabBarItems: List<UIKitUITabBarItem> = ScaffoldTab.entries.map { it.iosItem }

/**
 * [AdaptiveScaffold] with an [AdaptiveTopBar] and an [AdaptiveNavigationBar]. The content area is
 * outlined by two guides that sit exactly on its top and bottom edges: a stale bar padding (after
 * rotating the device, for example) shows up as a gap between a guide and its bar, or as a guide
 * hidden under it.
 */
@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun ScaffoldDemoScreen(navigateBack: () -> Unit) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(ScaffoldTab.Home.ordinal) }
    val selectedTab = ScaffoldTab.entries[selectedIndex]

    AdaptiveScaffold(
        topBar = { ScaffoldDemoTopBar(tab = selectedTab, navigateBack = navigateBack) },
        bottomBar = {
            ScaffoldDemoTabBar(selectedTab = selectedTab, onSelect = { selectedIndex = it.ordinal })
        },
    ) { padding ->
        ScaffoldDemoContent(tab = selectedTab, padding = padding)
    }
}

@Composable
private fun ScaffoldDemoContent(tab: ScaffoldTab, padding: PaddingValues) {
    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                fadeIn(animationSpec = tween(SCAFFOLD_TAB_FADE_MILLIS)) togetherWith
                    fadeOut(animationSpec = tween(SCAFFOLD_TAB_FADE_MILLIS))
            },
            modifier = Modifier.fillMaxSize(),
        ) { shownTab ->
            ScaffoldInsetsPage(tabTitle = shownTab.title, padding = padding)
        }
        // Drawn over the page so they stay on the edges while it scrolls.
        ContentEdgeGuide(
            edge = ContentEdge.Top,
            label = "Content top · ${padding.calculateTopPadding().toInsetLabel()}",
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
        )
        ContentEdgeGuide(
            edge = ContentEdge.Bottom,
            label = "Content bottom · ${padding.calculateBottomPadding().toInsetLabel()}",
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalCalfUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ScaffoldDemoTopBar(tab: ScaffoldTab, navigateBack: () -> Unit) {
    AdaptiveTopBar(
        title = { Text(tab.title) },
        navigationIcon = {
            AdaptiveIconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        iosTitle = tab.title,
        iosLeadingItems = listOf(
            UIKitUIBarButtonItem(title = "Back", onClick = navigateBack),
        ),
    )
}

@OptIn(ExperimentalCalfUiApi::class)
@Composable
private fun ScaffoldDemoTabBar(selectedTab: ScaffoldTab, onSelect: (ScaffoldTab) -> Unit) {
    AdaptiveNavigationBar(
        iosItems = ScaffoldTabBarItems,
        iosSelectedIndex = selectedTab.ordinal,
        iosOnItemSelected = { index -> ScaffoldTab.entries.getOrNull(index)?.let(onSelect) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        ScaffoldTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(tab.title) },
            )
        }
    }
}
