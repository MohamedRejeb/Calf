@file:OptIn(
    com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.adaptiveNavEntry
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItemVisibilityPriority
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonSystemItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlinx.serialization.Serializable

/** Pushed from the Explore tab: a bar with more items than a vertical strip can hold. */
@Serializable
internal data object NavDemoBarItems : NavKey

/** One bar item of the demo, listed in the content next to its priority. */
private data class NavDemoBarItem(
    val name: String,
    val priority: UIKitUIBarButtonItemVisibilityPriority,
    val kind: String,
)

private val demoBarItems = listOf(
    NavDemoBarItem("Add item", UIKitUIBarButtonItemVisibilityPriority.High, "system item"),
    NavDemoBarItem("Favorite", UIKitUIBarButtonItemVisibilityPriority.Standard, "SF Symbol"),
    NavDemoBarItem("Bookmark", UIKitUIBarButtonItemVisibilityPriority.Standard, "SF Symbol"),
    NavDemoBarItem("Notifications", UIKitUIBarButtonItemVisibilityPriority.Low, "Compose vector, needs a label"),
    NavDemoBarItem("Sort", UIKitUIBarButtonItemVisibilityPriority.Low, "text item, stays in the title row on iPhone Duo"),
    NavDemoBarItem("Search", UIKitUIBarButtonItemVisibilityPriority.Standard, "system item"),
    NavDemoBarItem("Settings", UIKitUIBarButtonItemVisibilityPriority.Low, "SF Symbol"),
    NavDemoBarItem("Share", UIKitUIBarButtonItemVisibilityPriority.High, "SF Symbol"),
)

/**
 * Eight trailing items with mixed labels and visibility priorities. On an iPhone Duo the
 * image items move into the vertical strip; when it runs out of room the `Low` items go
 * to the overflow menu first and the `High` items last. Every image item has a label, so
 * VoiceOver and the overflow menu can name it.
 */
internal fun navDemoBarItemsEntry(
    key: NavDemoBarItems,
    backStack: NavBackStack<NavKey>,
    onTapped: (String) -> Unit,
    lastTapped: () -> String?,
): NavEntry<NavKey> = adaptiveNavEntry(
    key = key,
    title = { Text("Bar items") },
    navigationIcon = { NavDemoBackButton { backStack.removeLastOrNull() } },
    actions = {
        IconButton(onClick = { onTapped("Add item") }) { Icon(Icons.Filled.Add, contentDescription = "Add item") }
        IconButton(onClick = { onTapped("Favorite") }) { Icon(Icons.Outlined.Star, contentDescription = "Favorite") }
        IconButton(onClick = { onTapped("Share") }) { Icon(Icons.Outlined.Share, contentDescription = "Share") }
    },
    iosTitle = "Bar items",
    iosTrailingItems = listOf(
        UIKitUIBarButtonItem.systemItem(
            systemItem = UIKitUIBarButtonSystemItem.Add,
            label = "Add item",
            visibilityPriority = UIKitUIBarButtonItemVisibilityPriority.High,
            onClick = { onTapped("Add item") },
        ),
        UIKitUIBarButtonItem.image(
            image = UIKitImage.SystemName("star"),
            label = "Favorite",
            onClick = { onTapped("Favorite") },
        ),
        UIKitUIBarButtonItem.image(
            image = UIKitImage.SystemName("bookmark"),
            label = "Bookmark",
            onClick = { onTapped("Bookmark") },
        ),
        UIKitUIBarButtonItem.image(
            image = UIKitImage.Vector(Icons.Outlined.Notifications),
            label = "Notifications",
            visibilityPriority = UIKitUIBarButtonItemVisibilityPriority.Low,
            onClick = { onTapped("Notifications") },
        ),
        UIKitUIBarButtonItem.title(
            title = "Sort",
            visibilityPriority = UIKitUIBarButtonItemVisibilityPriority.Low,
            onClick = { onTapped("Sort") },
        ),
        UIKitUIBarButtonItem.systemItem(
            systemItem = UIKitUIBarButtonSystemItem.Search,
            label = "Search",
            onClick = { onTapped("Search") },
        ),
        UIKitUIBarButtonItem.image(
            image = UIKitImage.SystemName("gearshape"),
            label = "Settings",
            visibilityPriority = UIKitUIBarButtonItemVisibilityPriority.Low,
            onClick = { onTapped("Settings") },
        ),
        UIKitUIBarButtonItem.image(
            image = UIKitImage.SystemName("square.and.arrow.up"),
            label = "Share",
            visibilityPriority = UIKitUIBarButtonItemVisibilityPriority.High,
            onClick = { onTapped("Share") },
        ),
    ),
) { padding ->
    NavDemoBarItemsContent(contentPadding = padding, lastTapped = lastTapped)
}

@Composable
private fun NavDemoBarItemsContent(
    contentPadding: PaddingValues,
    lastTapped: () -> String?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item {
            NavDemoCard(
                title = "Overflow on iPhone Duo",
                body = "In the vertical bar of an iPhone Duo the image items share the strip with the " +
                    "tab bar. When there is no room for all of them, Low items move to the overflow " +
                    "menu first and High items stay the longest. Text items keep their place in the " +
                    "title row. Before iOS 27 the priority is ignored.",
            )
        }
        item {
            NavDemoCard(
                title = "Labels",
                body = "System items and SF Symbols carry a name of their own; a Compose vector or " +
                    "bitmap does not, so it gets a label. VoiceOver reads the label and the overflow " +
                    "menu shows it next to the image. Last tapped: ${lastTapped() ?: "nothing yet"}.",
            )
        }
        item { NavDemoSectionHeader("Trailing items, in order") }
        items(demoBarItems) { item ->
            ListItem(
                headlineContent = { Text(item.name) },
                supportingContent = { Text(item.kind) },
                trailingContent = { Text(item.priority.name) },
            )
            HorizontalDivider()
        }
    }
}

/** Remembers which bar item was tapped last, shown by the content. */
@Composable
internal fun rememberNavDemoLastTapped(): NavDemoLastTapped = remember { NavDemoLastTapped() }

internal class NavDemoLastTapped {
    var value: String? by mutableStateOf(null)
}
