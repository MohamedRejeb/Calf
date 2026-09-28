package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

private const val TOP_BAR_USAGE = """
AdaptiveTopBar(
    title = { Text("Top Bar") },
    navigationIcon = {
        IconButton(onClick = navigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    },
    iosTitle = "Top Bar",
    iosLeadingItems = listOf(
        UIKitUIBarButtonItem.back(onClick = navigateBack),
    ),
    iosTrailingItems = listOf(
        UIKitUIBarButtonItem(
            image = UIKitImage.SystemName(SFSymbol.squareAndArrowUp),
            label = "Share",
            onClick = { share() },
        ),
    ),
    iosConfiguration = UIKitNavigationBarConfiguration(prefersLargeTitles = true),
)
"""

private const val ACTION_SHARE = "Share"
private const val ACTION_MORE = "More"
private const val NO_ACTION = "None yet"

/**
 * A demo whose subject is its own bar: an [AdaptiveTopBar] with a back button, two actions and,
 * on iOS, a large title that can be switched off. The body reports which action was tapped, so
 * both the Material actions and the UIBarButtonItems can be seen working.
 */
@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun TopBarDemoScreen(navigateBack: () -> Unit) {
    var lastAction by rememberSaveable { mutableStateOf(NO_ACTION) }
    var largeTitle by rememberSaveable { mutableStateOf(true) }

    AdaptiveScaffold(
        topBar = {
            TopBarDemoBar(
                largeTitle = largeTitle,
                navigateBack = navigateBack,
                onAction = { lastAction = it },
            )
        },
    ) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.TopBarDemo)
            DemoSection(title = "Try it", footer = "Tap Share or More in the bar above.") {
                DemoCard {
                    ValueRow(label = "Last tapped", value = lastAction)
                }
            }
            BarItemsSection()
            DemoSection(
                title = "Options",
                footer = "iOS only: the title starts large under the bar and shrinks into it.",
            ) {
                DemoListCard {
                    DemoRow(
                        title = "Large title",
                        subtitle = "prefersLargeTitles",
                        trailing = { AdaptiveSwitch(checked = largeTitle, onCheckedChange = { largeTitle = it }) },
                    )
                }
            }
            PlatformsSection()
            DemoSection(title = "Usage") {
                CodeSnippet(TOP_BAR_USAGE.trimIndent())
            }
        }
    }
}

@OptIn(ExperimentalCalfUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TopBarDemoBar(
    largeTitle: Boolean,
    navigateBack: () -> Unit,
    onAction: (String) -> Unit,
) {
    AdaptiveTopBar(
        title = { Text(Screen.TopBarDemo.title) },
        navigationIcon = {
            AdaptiveIconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            AdaptiveIconButton(onClick = { onAction(ACTION_SHARE) }) {
                Icon(Icons.Filled.Share, contentDescription = ACTION_SHARE)
            }
            AdaptiveIconButton(onClick = { onAction(ACTION_MORE) }) {
                Icon(Icons.Filled.MoreVert, contentDescription = ACTION_MORE)
            }
        },
        iosTitle = Screen.TopBarDemo.title,
        iosLeadingItems = listOf(
            UIKitUIBarButtonItem.back(onClick = navigateBack),
        ),
        iosTrailingItems = listOf(
            UIKitUIBarButtonItem(
                image = UIKitImage.SystemName(SFSymbol.squareAndArrowUp),
                label = ACTION_SHARE,
                onClick = { onAction(ACTION_SHARE) },
            ),
            UIKitUIBarButtonItem(
                image = UIKitImage.SystemName(SFSymbol.ellipsisCircle),
                label = ACTION_MORE,
                onClick = { onAction(ACTION_MORE) },
            ),
        ),
        iosConfiguration = UIKitNavigationBarConfiguration(prefersLargeTitles = largeTitle),
    )
}

@Composable
private fun BarItemsSection() {
    DemoSection(
        title = "Bar items",
        footer = "On iOS each item is a UIBarButtonItem. Share and More use SF Symbols and a " +
            "label for VoiceOver.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Back",
                subtitle = "Leading · closes the demo",
                leading = { IconTile(icon = Icons.AutoMirrored.Filled.ArrowBack, color = TileIndigo) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DemoRow(
                title = ACTION_SHARE,
                subtitle = "Trailing · square.and.arrow.up",
                leading = { IconTile(icon = Icons.Outlined.Share, color = TileBlue) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DemoRow(
                title = ACTION_MORE,
                subtitle = "Trailing · ellipsis.circle",
                leading = { IconTile(icon = Icons.Outlined.MoreHoriz, color = TileViolet) },
            )
        }
    }
}

@Composable
private fun PlatformsSection() {
    DemoSection(title = "On each platform") {
        DemoListCard {
            DemoRow(
                title = "Android, desktop and web",
                subtitle = "Material 3 TopAppBar: navigationIcon, title and actions, with scroll " +
                    "behavior and expanded height",
            )
            DemoDivider()
            DemoRow(
                title = "iOS",
                subtitle = "UINavigationBar: iosTitle, iosLeadingItems, iosTrailingItems and " +
                    "iosConfiguration",
            )
        }
    }
}
