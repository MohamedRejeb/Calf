package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.AdaptivePaneRole
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.navigation.adaptiveNavEntry
import com.mohamedrejeb.calf.navigation.selectDetail
import kotlinx.serialization.Serializable
import kotlin.random.Random

// The "Mail" tab: a list, a detail, and a screen the detail pushes. One back stack, laid out as
// two panes when there is room (iPad, open iPhone Duo, unfolded foldable, wide window).
@Serializable
internal data object NavDemoInbox : NavKey

@Serializable
internal data class NavDemoMessage(val messageId: Int) : NavKey

@Serializable
internal data class NavDemoAttachment(val messageId: Int) : NavKey

/** Proves that a layout change keeps the ViewModel: its number never changes while the entry lives. */
internal class NavDemoInstanceViewModel : ViewModel() {
    val instance: Int = Random.nextInt(from = 1000, until = 10_000)
}

/** The entry for a key of the Mail tab, or `null` for a key that belongs to another tab. */
@OptIn(ExperimentalCalfNavigationApi::class)
internal fun navDemoListDetailEntry(key: NavKey, backStack: NavBackStack<NavKey>): NavEntry<NavKey>? =
    when (key) {
        is NavDemoInbox -> adaptiveNavEntry(
            key = key,
            title = { Text("Inbox") },
            largeTitle = true,
            iosTitle = "Inbox",
            iosBackTitle = "Inbox",
            paneRole = AdaptivePaneRole.List(detailPlaceholder = { NavDemoNoMessageSelected() }),
        ) { padding ->
            NavDemoInboxContent(
                contentPadding = padding,
                // Read in the composition, so the highlight follows the back stack on every platform.
                selectedMessageId = backStack.filterIsInstance<NavDemoMessage>().lastOrNull()?.messageId,
                onOpenMessage = { id -> backStack.selectDetail(list = NavDemoInbox, detail = NavDemoMessage(id)) },
            )
        }

        is NavDemoMessage -> adaptiveNavEntry(
            key = key,
            title = { Text("Message ${key.messageId}") },
            navigationIcon = { NavDemoListDetailBackButton { backStack.removeLastOrNull() } },
            iosTitle = "Message ${key.messageId}",
            iosBackTitle = "Message",
            paneRole = AdaptivePaneRole.Detail,
        ) { padding ->
            NavDemoStateProbeContent(
                title = "Message ${key.messageId}",
                contentPadding = padding,
                actionLabel = "Open attachment",
                onAction = { backStack.add(NavDemoAttachment(key.messageId)) },
            )
        }

        is NavDemoAttachment -> adaptiveNavEntry(
            key = key,
            title = { Text("Attachment") },
            navigationIcon = { NavDemoListDetailBackButton { backStack.removeLastOrNull() } },
            iosTitle = "Attachment",
        ) { padding ->
            NavDemoStateProbeContent(
                title = "Attachment of message ${key.messageId}",
                contentPadding = padding,
                actionLabel = "Back to the message",
                onAction = { backStack.removeLastOrNull() },
            )
        }

        else -> null
    }

@Composable
private fun NavDemoInboxContent(
    contentPadding: PaddingValues,
    selectedMessageId: Int?,
    onOpenMessage: (Int) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(count = MESSAGE_COUNT, key = { index -> index }) { index ->
            val id = index + 1
            ListItem(
                headlineContent = { Text("Message $id") },
                supportingContent = { Text("Tap to open. In two panes the inbox stays where it is.") },
                tonalElevation = if (id == selectedMessageId) 4.dp else 0.dp,
                modifier = Modifier.fillMaxWidth().clickable { onOpenMessage(id) },
            )
            HorizontalDivider()
        }
    }
}

/**
 * A scrollable screen that shows what has to survive a layout change: the scroll position, a saved
 * counter and the ViewModel instance.
 */
@Composable
private fun NavDemoStateProbeContent(
    title: String,
    contentPadding: PaddingValues,
    actionLabel: String,
    onAction: () -> Unit,
) {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    val instance = viewModel { NavDemoInstanceViewModel() }.instance

    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        item { Text(title, style = MaterialTheme.typography.headlineSmall) }
        item { Text("ViewModel instance #$instance · saved taps: $taps") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { taps += 1 }) { Text("Tap ($taps)") }
                Button(onClick = onAction) { Text(actionLabel) }
            }
        }
        items(PROBE_LINE_COUNT) { index ->
            Text("Line ${index + 1} · scroll, then open or close the fold, rotate, or resize the window")
        }
    }
}

@Composable
private fun NavDemoNoMessageSelected() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select a message", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun NavDemoListDetailBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}

private const val MESSAGE_COUNT = 40
private const val PROBE_LINE_COUNT = 60
