@file:OptIn(
    com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.mohamedrejeb.calf.navigation.AdaptiveNavDisplay
import com.mohamedrejeb.calf.navigation.AdaptiveTabDisplay
import com.mohamedrejeb.calf.navigation.adaptiveNavEntry
import com.mohamedrejeb.calf.navigation.adaptiveTab
import com.mohamedrejeb.calf.navigation.rememberAdaptiveListDetailSettings
import com.mohamedrejeb.calf.navigation.rememberAdaptiveNavBackStack
import com.mohamedrejeb.calf.navigation.rememberAdaptiveTabState
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonSystemItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

// Outer stack: the tabs, and a screen pushed over them (sign-out flow).
@Serializable
internal data object NavDemoRoot : NavKey

@Serializable
internal data object NavDemoSignedOut : NavKey

// Tabs.
@Serializable
internal data object NavDemoHome : NavKey

@Serializable
internal data object NavDemoExplore : NavKey

@Serializable
internal data object NavDemoSettings : NavKey

// Screens pushed inside a tab.
@Serializable
internal data class NavDemoDetail(val itemId: Int) : NavKey

@Serializable
internal data object NavDemoCover : NavKey

@Serializable
internal data object NavDemoLocked : NavKey

/**
 * Persists the demo's back stacks across Android configuration changes and
 * process death. Every [NavKey] that can be on a stack has to be registered.
 */
private val NavDemoSavedState = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(NavDemoRoot::class)
            subclass(NavDemoSignedOut::class)
            subclass(NavDemoHome::class)
            subclass(NavDemoExplore::class)
            subclass(NavDemoSettings::class)
            subclass(NavDemoDetail::class)
            subclass(NavDemoCover::class)
            subclass(NavDemoLocked::class)
            subclass(NavDemoBarItems::class)
            subclass(NavDemoInbox::class)
            subclass(NavDemoMessage::class)
            subclass(NavDemoAttachment::class)
        }
    }
}

/**
 * Full-screen showcase for [AdaptiveTabDisplay] and [AdaptiveNavDisplay]. It
 * owns the whole screen: the navigators paint the only bars, so it is not
 * nested inside the sample app's scaffold.
 *
 * What it demonstrates:
 *  - A native tab bar (`UITabBarController` on iOS, Material navigation
 *    suite elsewhere) with one navigation stack per tab; each tab keeps its
 *    stack, scroll position and ViewModels while another tab is shown.
 *  - Large titles that collapse on scroll, native bar items, push/pop with
 *    duplicates and pop-to-root, re-selecting a tab pops it to the root.
 *  - `tabBarShown = false`: the tab bar leaves with the push transition.
 *  - `barShown = false` and `iosGestureEnabled = false` per entry.
 *  - Signing out pushes a screen *over* the tabs from an outer stack, so the
 *    tab bar rides the transition and the back swipe (issue #530).
 *  - `iosSidebarAdaptable`: the tab bar becomes a sidebar on iPad and on the
 *    inner display of iPhone Duo.
 *  - `listDetail`: the Mail tab is one back stack shown as two panes when there
 *    is room and as a normal push when there is not; see
 *    `AdaptiveNavigationDemoListDetail.kt`.
 */
@Composable
fun AdaptiveNavigationDemoScreen(navigateBack: () -> Unit) {
    val rootStack = rememberAdaptiveNavBackStack(NavDemoRoot, configuration = NavDemoSavedState)

    AdaptiveNavDisplay(
        backStack = rootStack,
        modifier = Modifier.fillMaxSize(),
    ) { key ->
        when (key) {
            is NavDemoRoot -> adaptiveNavEntry(
                key = key,
                barShown = false,
            ) {
                NavDemoTabs(
                    onExit = navigateBack,
                    onSignOut = { rootStack.add(NavDemoSignedOut) },
                )
            }

            is NavDemoSignedOut -> adaptiveNavEntry(
                key = key,
                title = { Text("Signed out") },
                navigationIcon = { NavDemoBackButton { rootStack.removeLastOrNull() } },
                iosTitle = "Signed out",
            ) { padding ->
                NavDemoSignedOutContent(
                    contentPadding = padding,
                    onSignIn = { rootStack.removeLastOrNull() },
                )
            }

            else -> error("Unexpected root key: $key")
        }
    }
}

@Composable
private fun NavDemoTabs(
    onExit: () -> Unit,
    onSignOut: () -> Unit,
) {
    // <NavKey>: on Native and JS the serialization plugin adds a supertype to @Serializable
    // objects, and the inferred tab type would no longer be plain NavKey.
    val tabs = rememberAdaptiveTabState<NavKey>(
        NavDemoHome,
        NavDemoInbox,
        NavDemoExplore,
        NavDemoSettings,
        configuration = NavDemoSavedState,
    )
    var itemCount by remember { mutableIntStateOf(5) }
    val lastTapped = rememberNavDemoLastTapped()

    AdaptiveTabDisplay(
        state = tabs,
        tabs = listOf(
            adaptiveTab(
                key = NavDemoHome,
                icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                label = { Text("Home") },
                iosTitle = "Home",
                iosImage = UIKitImage.SystemName("house"),
                iosSelectedImage = UIKitImage.SystemName("house.fill"),
            ),
            adaptiveTab(
                key = NavDemoInbox,
                icon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                label = { Text("Mail") },
                iosTitle = "Mail",
                iosImage = UIKitImage.SystemName("envelope"),
                iosSelectedImage = UIKitImage.SystemName("envelope.fill"),
            ),
            adaptiveTab(
                key = NavDemoExplore,
                icon = { Icon(Icons.Outlined.Explore, contentDescription = null) },
                label = { Text("Explore") },
                badge = "2",
                iosTitle = "Explore",
                iosImage = UIKitImage.SystemName("safari"),
                iosSelectedImage = UIKitImage.SystemName("safari.fill"),
            ),
            adaptiveTab(
                key = NavDemoSettings,
                icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                label = { Text("Settings") },
                iosTitle = "Settings",
                iosImage = UIKitImage.SystemName("gearshape"),
                iosSelectedImage = UIKitImage.SystemName("gearshape.fill"),
            ),
        ),
        modifier = Modifier.fillMaxSize(),
        iosSidebarAdaptable = true,
        listDetail = rememberAdaptiveListDetailSettings(),
    ) { key ->
        when (key) {
            is NavDemoHome -> adaptiveNavEntry(
                key = key,
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit demo")
                    }
                },
                actions = {
                    IconButton(onClick = { itemCount += 1 }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add item")
                    }
                },
                largeTitle = true,
                iosTitle = "Home",
                iosBackTitle = "Home",
                iosLeadingItems = listOf(
                    UIKitUIBarButtonItem.systemItem(UIKitUIBarButtonSystemItem.Close, onClick = onExit),
                ),
                iosTrailingItems = listOf(
                    UIKitUIBarButtonItem.systemItem(UIKitUIBarButtonSystemItem.Add, onClick = { itemCount += 1 }),
                ),
            ) { padding ->
                NavDemoHomeContent(
                    contentPadding = padding,
                    itemCount = { itemCount },
                    onOpenItem = { tabs.backStack.add(NavDemoDetail(it)) },
                )
            }

            is NavDemoDetail -> adaptiveNavEntry(
                key = key,
                title = { Text("Item ${key.itemId}") },
                navigationIcon = { NavDemoBackButton { tabs.backStack.removeLastOrNull() } },
                iosTitle = "Item ${key.itemId}",
                iosBackTitle = "Item ${key.itemId}",
                tabBarShown = false,
            ) { padding ->
                NavDemoDetailContent(
                    itemId = key.itemId,
                    contentPadding = padding,
                    backStack = tabs.backStack,
                    onPopToRoot = { tabs.popToRoot() },
                )
            }

            is NavDemoExplore -> adaptiveNavEntry(
                key = key,
                title = { Text("Explore") },
                largeTitle = true,
                iosTitle = "Explore",
                iosBackTitle = "Explore",
            ) { padding ->
                NavDemoExploreContent(
                    contentPadding = padding,
                    onOpenCover = { tabs.backStack.add(NavDemoCover) },
                    onOpenLocked = { tabs.backStack.add(NavDemoLocked) },
                    onOpenBarItems = { tabs.backStack.add(NavDemoBarItems) },
                )
            }

            is NavDemoBarItems -> navDemoBarItemsEntry(
                key = key,
                backStack = tabs.backStack,
                onTapped = { lastTapped.value = it },
                lastTapped = { lastTapped.value },
            )

            is NavDemoCover -> adaptiveNavEntry(
                key = key,
                barShown = false,
                tabBarShown = false,
                iosTitle = "Cover",
            ) { padding ->
                NavDemoCoverContent(
                    contentPadding = padding,
                    onClose = { tabs.backStack.removeLastOrNull() },
                )
            }

            is NavDemoLocked -> adaptiveNavEntry(
                key = key,
                title = { Text("No Swipe Back") },
                navigationIcon = { NavDemoBackButton { tabs.backStack.removeLastOrNull() } },
                iosTitle = "No Swipe Back",
                iosGestureEnabled = false,
            ) { padding ->
                NavDemoLockedContent(
                    contentPadding = padding,
                    onBack = { tabs.backStack.removeLastOrNull() },
                )
            }

            is NavDemoSettings -> adaptiveNavEntry(
                key = key,
                title = { Text("Settings") },
                largeTitle = true,
                iosTitle = "Settings",
            ) { padding ->
                NavDemoSettingsContent(
                    contentPadding = padding,
                    tabs = tabs,
                    onSignOut = onSignOut,
                )
            }

            // The Mail tab's own stack: `tabs.backStack` is the *selected* tab's, and on iOS the
            // entries of every tab are built up front, while another tab is selected.
            else -> navDemoListDetailEntry(key, tabs.backStackOf(NavDemoInbox)) ?: error("Unexpected tab key: $key")
        }
    }
}

@Composable
internal fun NavDemoBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}

internal fun NavBackStack<NavKey>.demoLabels(): String = joinToString(" › ") { it.demoLabel() }

private fun NavKey.demoLabel(): String =
    when (this) {
        is NavDemoRoot -> "Tabs"
        is NavDemoSignedOut -> "Signed out"
        is NavDemoHome -> "Home"
        is NavDemoExplore -> "Explore"
        is NavDemoSettings -> "Settings"
        is NavDemoDetail -> "Item $itemId"
        is NavDemoCover -> "Cover"
        is NavDemoLocked -> "Locked"
        is NavDemoBarItems -> "Bar items"
        is NavDemoInbox -> "Inbox"
        is NavDemoMessage -> "Message $messageId"
        is NavDemoAttachment -> "Attachment"
        else -> toString()
    }
