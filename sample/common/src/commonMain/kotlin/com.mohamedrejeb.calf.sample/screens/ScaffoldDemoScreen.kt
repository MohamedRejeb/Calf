package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.mohamedrejeb.calf.sf.symbols.SFSymbol

@OptIn(ExperimentalCalfUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldDemoScreen(
    navigateBack: () -> Unit,
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Favorites", "Settings")
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.Favorite,
        Icons.Outlined.Settings,
    )
    val selectedIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Favorite,
        Icons.Filled.Settings,
    )

    AdaptiveScaffold(
        topBar = {
            AdaptiveTopBar(
                title = { Text(tabs[selectedIndex]) },
                navigationIcon = {
                    AdaptiveIconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                iosTitle = tabs[selectedIndex],
                iosLeadingItems = listOf(
                    UIKitUIBarButtonItem(
                        title = "Back",
                        onClick = navigateBack,
                    )
                ),
            )
        },
        bottomBar = {
            AdaptiveNavigationBar(
                iosItems = listOf(
                    UIKitUITabBarItem(
                        title = "Home",
                        image = UIKitImage.SystemName(SFSymbol.houseFill),
                    ),
                    UIKitUITabBarItem(
                        title = "Favorites",
                        image = UIKitImage.Vector(Icons.Rounded.Favorite),
                    ),
                    UIKitUITabBarItem(
                        title = "Settings",
                        image = UIKitImage.Vector(Icons.Rounded.Settings),
                    ),
                ),
                iosSelectedIndex = selectedIndex,
                iosOnItemSelected = { selectedIndex = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                if (selectedIndex == index) selectedIcons[index] else icons[index],
                                contentDescription = tab,
                            )
                        },
                        label = { Text(tab) },
                    )
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier.fillMaxSize(),
        ) { tabIndex ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // The markers sit exactly on the edges of the padded content, so a stale bar
                // padding (e.g. after rotating the device) shows up as a gap or an overlap.
                ContentEdgeMarker(
                    text = "Content top edge: must touch the top bar",
                    modifier = Modifier.align(Alignment.TopCenter),
                )
                ContentEdgeMarker(
                    text = "Content bottom edge: must touch the tab bar",
                    modifier = Modifier.align(Alignment.BottomCenter),
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                ) {
                    Text(
                        text = tabs[tabIndex],
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "This demonstrates AdaptiveScaffold + AdaptiveTopBar + AdaptiveNavigationBar working together. On iOS, native UINavigationBar and UITabBar are used. Rotate the device: the markers must stay glued to the bars.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentEdgeMarker(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 2.dp),
    )
}
