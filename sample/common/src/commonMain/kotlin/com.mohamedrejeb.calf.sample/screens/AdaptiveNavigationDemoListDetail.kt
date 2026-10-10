package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.AdaptivePaneRole
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.navigation.adaptiveNavEntry
import com.mohamedrejeb.calf.navigation.selectDetail
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.EmptyState
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
            paneRole = AdaptivePaneRole.List(detailPlaceholder = { padding -> NavDemoNoMessageSelected(padding) }),
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
            title = { Text(navDemoMail(key.messageId).sender) },
            navigationIcon = { NavDemoBackButton { backStack.removeLastOrNull() } },
            iosTitle = navDemoMail(key.messageId).sender,
            iosBackTitle = "Message",
            paneRole = AdaptivePaneRole.Detail,
        ) { padding ->
            NavDemoMessageContent(
                messageId = key.messageId,
                contentPadding = padding,
                onOpenAttachment = { backStack.add(NavDemoAttachment(key.messageId)) },
            )
        }

        is NavDemoAttachment -> adaptiveNavEntry(
            key = key,
            title = { Text("Attachment") },
            navigationIcon = { NavDemoBackButton { backStack.removeLastOrNull() } },
            iosTitle = "Attachment",
        ) { padding ->
            NavDemoAttachmentContent(
                messageId = key.messageId,
                contentPadding = padding,
                onBack = { backStack.removeLastOrNull() },
            )
        }

        else -> null
    }

/** The inbox. The open message is highlighted, which shows in two panes. */
@Composable
internal fun NavDemoInboxContent(
    contentPadding: PaddingValues,
    selectedMessageId: Int?,
    onOpenMessage: (Int) -> Unit,
) {
    DemoPage(contentPadding = contentPadding) {
        DemoListCard {
            navDemoMailIds.forEach { id ->
                if (id != navDemoMailIds.first) DemoDivider(startIndent = NavDemoThumbnailIndent)
                NavDemoMailRow(
                    mail = navDemoMail(id),
                    selected = id == selectedMessageId,
                    onClick = { onOpenMessage(id) },
                )
            }
        }
    }
}

/**
 * The state probe of an entry of this tab: taps saved with `rememberSaveable` and the number of
 * the entry's ViewModel. Both, and the page's scroll position, must survive a layout change.
 */
@Composable
internal fun NavDemoMailStateCheck() {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    val instance = viewModel { NavDemoInstanceViewModel() }.instance

    NavDemoStateCheck(
        taps = taps,
        instance = instance,
        onTap = { taps += 1 },
        footer = "Scroll, then open or close the fold, rotate, or resize the window. The same numbers " +
            "and scroll position mean the entry was kept.",
    )
}

@Composable
private fun NavDemoNoMessageSelected(contentPadding: PaddingValues) {
    // Background before the padding: it fills the pane, the content keeps clear of the bars.
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding),
    ) {
        EmptyState(
            icon = Icons.Outlined.Email,
            title = "No message selected",
            message = "Choose a message in the inbox to read it here.",
        )
    }
}

