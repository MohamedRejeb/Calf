@file:OptIn(com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi::class)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.automirrored.outlined.ViewSidebar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.AdaptiveTabState
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.InitialsAvatar
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet

private val SelectedMarkSize = 20.dp

private const val ACCOUNT_NAME = "Alex Morgan"
private const val ACCOUNT_EMAIL = "alex.morgan@example.com"

/** How the Settings tab lists a tab of the demo. */
private data class NavDemoTabInfo(val label: String, val icon: ImageVector, val tint: Color)

private fun NavKey.tabInfo(): NavDemoTabInfo =
    when (this) {
        is NavDemoHome -> NavDemoTabInfo("Home", Icons.Outlined.Home, TileIndigo)
        is NavDemoInbox -> NavDemoTabInfo("Mail", Icons.Outlined.Email, TileBlue)
        is NavDemoExplore -> NavDemoTabInfo("Explore", Icons.Outlined.Explore, TileCoral)
        else -> NavDemoTabInfo("Settings", Icons.Outlined.Settings, TileViolet)
    }

/**
 * The Settings tab: the tab state read live, selecting a tab from code, and signing out, which
 * pushes a screen over the tabs from the outer stack.
 */
@Composable
internal fun NavDemoSettingsContent(
    contentPadding: PaddingValues,
    tabs: AdaptiveTabState<NavKey>,
    onSignOut: () -> Unit,
) {
    DemoPage(contentPadding = contentPadding) {
        DemoListCard {
            DemoRow(
                title = ACCOUNT_NAME,
                subtitle = ACCOUNT_EMAIL,
                leading = {
                    InitialsAvatar(
                        name = ACCOUNT_NAME,
                        color = TileIndigo,
                        size = NavDemoThumbnailSize,
                        textStyle = MaterialTheme.typography.titleSmall,
                    )
                },
            )
        }
        NavDemoTabStateSection(tabs)
        DemoSection(
            title = "From code",
            footer = "The same as tapping the tab, transition included.",
        ) {
            DemoListCard {
                DemoRow(
                    title = "Select the Home tab",
                    subtitle = "tabs.select(NavDemoHome)",
                    leading = { IconTile(icon = Icons.Outlined.TouchApp, color = TileTeal) },
                    onClick = { tabs.select(NavDemoHome) },
                )
            }
        }
        DemoSection(
            title = "Wide displays",
            footer = "The tab bar becomes a sidebar on iPad and on the inner display of iPhone Duo. " +
                "On Material the navigation suite switches to a rail in wide windows.",
        ) {
            DemoListCard {
                DemoRow(
                    title = "Sidebar",
                    subtitle = "iosSidebarAdaptable = true",
                    leading = { IconTile(icon = Icons.AutoMirrored.Outlined.ViewSidebar, color = TileAmber) },
                )
            }
        }
        NavDemoSignOutSection(onSignOut)
    }
}

/** Every tab with its own back stack, read live from the [AdaptiveTabState]. */
@Composable
private fun NavDemoTabStateSection(tabs: AdaptiveTabState<NavKey>) {
    DemoSection(
        title = "Tabs",
        footer = "Each tab keeps its own back stack. Push a place on Home, come back here and it is listed.",
    ) {
        DemoListCard {
            tabs.tabs.forEachIndexed { index, tab ->
                if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                val info = tab.tabInfo()
                DemoRow(
                    title = info.label,
                    subtitle = tabs.backStackOf(tab).stackSummary(),
                    leading = { IconTile(icon = info.icon, color = info.tint) },
                    trailing = if (tab == tabs.selectedTab) {
                        { NavDemoSelectedMark() }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

/** "At the root", or the whole stack when something is pushed on it. */
private fun NavBackStack<NavKey>.stackSummary(): String =
    if (size <= 1) "At the root" else "$size screens · ${demoLabels()}"

@Composable
private fun NavDemoSelectedMark() {
    Icon(
        imageVector = Icons.Filled.Check,
        contentDescription = "Selected",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(SelectedMarkSize),
    )
}

@Composable
private fun NavDemoSignOutSection(onSignOut: () -> Unit) {
    DemoSection(
        title = "Account",
        footer = "Signing out pushes a screen over the tabs from the outer stack. The tab bar leaves " +
            "with the transition and comes back with the back swipe, on iOS as well.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Sign out",
                leading = { IconTile(icon = Icons.AutoMirrored.Outlined.Logout, color = TileCoral) },
                onClick = onSignOut,
            )
        }
    }
}

/** Pushed over the tabs from the outer stack. */
@Composable
internal fun NavDemoSignedOutContent(
    contentPadding: PaddingValues,
    onSignIn: () -> Unit,
) {
    NavDemoStatusPage(
        contentPadding = contentPadding,
        icon = Icons.Outlined.PersonOff,
        title = "You are signed out",
        message = "This screen is on the outer stack, above the tabs. Swipe back on iOS and the tab " +
            "bar comes back with the gesture.",
        actionLabel = "Sign in again",
        onAction = onSignIn,
    )
}
