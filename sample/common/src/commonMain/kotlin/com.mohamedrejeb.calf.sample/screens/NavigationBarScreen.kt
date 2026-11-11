package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitFont
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

private const val TAB_FADE_MILLIS = 300
private const val TAB_TITLE_FONT_SIZE = 11f

/** The tabs of the demo's travel app, with the Material icons of each. */
private enum class TravelTab(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home(
        title = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
    ),
    Saved(
        title = "Saved",
        icon = Icons.Outlined.FavoriteBorder,
        selectedIcon = Icons.Filled.Favorite,
    ),
    Profile(
        title = "Profile",
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person,
    ),
}

/**
 * The UITabBar item of each tab, in the order of [TravelTab]. They mix the three ways to give a
 * tab an image, so each one is exercised on a real bar.
 */
@Composable
private fun rememberTravelTabBarItems(): List<UIKitUITabBarItem> {
    val profilePainter = rememberVectorPainter(Icons.Outlined.Person)
    val profileSelectedPainter = rememberVectorPainter(Icons.Filled.Person)
    return remember(profilePainter, profileSelectedPainter) {
        TravelTab.entries.map { tab ->
            when (tab) {
                // SF Symbols, outlined at rest and filled when selected, like the system apps.
                TravelTab.Home -> UIKitUITabBarItem(
                    title = tab.title,
                    image = UIKitImage.SystemName(SFSymbol.house),
                    selectedImage = UIKitImage.SystemName(SFSymbol.houseFill),
                )
                // Compose vectors, rasterised into UIImages as one filled shape.
                TravelTab.Saved -> UIKitUITabBarItem(
                    title = tab.title,
                    image = UIKitImage.Vector(Icons.Rounded.FavoriteBorder),
                    selectedImage = UIKitImage.Vector(Icons.Rounded.Favorite),
                )
                // Painters keep everything a vector can draw (strokes, even-odd fills, groups);
                // painterResource(...) works the same way.
                TravelTab.Profile -> UIKitUITabBarItem(
                    title = tab.title,
                    image = UIKitImage.Painter(profilePainter),
                    selectedImage = UIKitImage.Painter(profileSelectedPainter),
                )
            }
        }
    }
}

/**
 * A small travel app with three tabs. The tab bar is an [AdaptiveNavigationBar] (a real UITabBar
 * on iOS) and the top bar an [AdaptiveTopBar] whose title follows the selected tab. Every tab is
 * padded by the scaffold on all four edges, so its content scrolls behind both bars and stops
 * clear of them, and of the display cutout in landscape.
 */
@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun NavigationBarScreen(navigateBack: () -> Unit) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(TravelTab.Home.ordinal) }
    val selectedTab = TravelTab.entries[selectedIndex]
    val selectTab: (TravelTab) -> Unit = { selectedIndex = it.ordinal }

    AdaptiveScaffold(
        topBar = {
            TravelTopBar(
                tab = selectedTab,
                navigateBack = navigateBack,
                // Search lives with the saved places, like in most travel apps.
                onSearch = { selectTab(TravelTab.Saved) },
            )
        },
        bottomBar = {
            TravelTabBar(selectedTab = selectedTab, onSelect = selectTab)
        },
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(TAB_FADE_MILLIS)) togetherWith
                    fadeOut(animationSpec = tween(TAB_FADE_MILLIS))
            },
            modifier = Modifier.fillMaxSize(),
        ) { tab ->
            when (tab) {
                TravelTab.Home -> TravelHomeTab(contentPadding = padding)
                TravelTab.Saved -> TravelSavedTab(contentPadding = padding)
                TravelTab.Profile -> TravelProfileTab(contentPadding = padding)
            }
        }
    }
}

@OptIn(ExperimentalCalfUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TravelTopBar(
    tab: TravelTab,
    navigateBack: () -> Unit,
    onSearch: () -> Unit,
) {
    AdaptiveTopBar(
        title = { Text(tab.title) },
        navigationIcon = {
            AdaptiveIconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            AdaptiveIconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        },
        iosTitle = tab.title,
        iosLeadingItems = listOf(
            UIKitUIBarButtonItem.back(onClick = navigateBack),
        ),
        iosTrailingItems = listOf(
            UIKitUIBarButtonItem(
                image = UIKitImage.SystemName(SFSymbol.magnifyingglass),
                label = "Search",
                onClick = onSearch,
            ),
        ),
    )
}

@OptIn(ExperimentalCalfUiApi::class)
@Composable
private fun TravelTabBar(
    selectedTab: TravelTab,
    onSelect: (TravelTab) -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    AdaptiveNavigationBar(
        iosItems = rememberTravelTabBarItems(),
        iosSelectedIndex = selectedTab.ordinal,
        iosOnItemSelected = { index -> TravelTab.entries.getOrNull(index)?.let(onSelect) },
        // The selected item in the brand color and the titles in a heavier system font.
        iosConfiguration = remember(primaryColor) {
            UIKitTabBarConfiguration(
                selectedIconColor = primaryColor,
                selectedTitleColor = primaryColor,
                titleFont = UIKitFont.System(size = TAB_TITLE_FONT_SIZE, weight = FontWeight.SemiBold),
            )
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        TravelTab.entries.forEach { tab ->
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
