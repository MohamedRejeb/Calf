package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.ui.theme.PageMaxWidth
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.ui.button.AdaptiveButton

private val CoverIconSize = 56.dp

/** The Explore tab: every option an entry can set on itself, one section each. */
@Composable
internal fun NavDemoExploreContent(
    contentPadding: PaddingValues,
    onOpenCover: () -> Unit,
    onOpenLocked: () -> Unit,
    onOpenBarItems: () -> Unit,
) {
    DemoPage(contentPadding = contentPadding) {
        NavDemoOptionSection(
            section = "Full screen",
            title = "Edge-to-edge cover",
            code = "barShown = false, tabBarShown = false",
            icon = Icons.Outlined.Fullscreen,
            tint = TileIndigo,
            footer = "Hides the navigation bar and the tab bar, so the content owns the whole screen. " +
                "On iOS the edge swipe still pops it.",
            onClick = onOpenCover,
        )
        NavDemoOptionSection(
            section = "Gestures",
            title = "No swipe back",
            code = "iosGestureEnabled = false",
            icon = Icons.Outlined.Lock,
            tint = TileAmber,
            footer = "Turns the iOS edge swipe off for one entry, for a form that should not be " +
                "dismissed by accident. The back button still works.",
            onClick = onOpenLocked,
        )
        NavDemoOptionSection(
            section = "Bar items",
            title = "Bar items and overflow",
            code = "label, visibilityPriority",
            icon = Icons.Outlined.MoreHoriz,
            tint = TileTeal,
            footer = "Eight trailing items. On an iPhone Duo the image items move to the vertical " +
                "strip and overflow by priority.",
            onClick = onOpenBarItems,
        )
        DemoSection(
            title = "Badges",
            footer = "The Explore tab carries a badge: a Material badge here, the native badgeValue on iOS.",
        ) {
            DemoListCard {
                DemoRow(
                    title = "Explore",
                    subtitle = "badge = \"2\"",
                    leading = { IconTile(icon = Icons.Outlined.Explore, color = TileCoral) },
                    trailing = { NavDemoBadge("2") },
                )
            }
        }
    }
}

@Composable
private fun NavDemoOptionSection(
    section: String,
    title: String,
    code: String,
    icon: ImageVector,
    tint: Color,
    footer: String,
    onClick: () -> Unit,
) {
    DemoSection(title = section, footer = footer) {
        DemoListCard {
            DemoRow(
                title = title,
                subtitle = code,
                leading = { IconTile(icon = icon, color = tint) },
                onClick = onClick,
            )
        }
    }
}

/** The count a tab badge shows, drawn the way a navigation bar draws it. */
@Composable
private fun NavDemoBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        shape = CircleShape,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs / 2),
        )
    }
}

/** A full-bleed page in the brand colour, with neither the navigation bar nor the tab bar. */
@Composable
internal fun NavDemoCoverContent(
    contentPadding: PaddingValues,
    onClose: () -> Unit,
) {
    // The brand indigo in both themes: a cover is a photo or a colour, not a themed surface.
    val background = TileIndigo
    val content = Color.White

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(contentPadding),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            modifier = Modifier
                .widthIn(max = PageMaxWidth)
                .padding(Spacing.xl),
        ) {
            Icon(
                imageVector = Icons.Outlined.Fullscreen,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(CoverIconSize),
            )
            Text(
                text = "Edge to edge",
                style = MaterialTheme.typography.headlineMedium,
                color = content,
            )
            Text(
                text = "This entry hides the navigation bar and the tab bar, so the content owns the " +
                    "whole screen. On iOS you can still swipe from the left edge to go back.",
                style = MaterialTheme.typography.bodyLarge,
                color = content,
                textAlign = TextAlign.Center,
            )
            AdaptiveButton(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = content, contentColor = background),
            ) {
                Text("Close")
            }
        }
    }
}

/** A form-like entry the iOS edge swipe cannot dismiss. */
@Composable
internal fun NavDemoLockedContent(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    NavDemoStatusPage(
        contentPadding = contentPadding,
        icon = Icons.Outlined.Lock,
        title = "Swipe back is off",
        message = "This entry sets iosGestureEnabled = false, so the iOS edge swipe does not pop it. " +
            "Use the back button in the bar, or the one below.",
        actionLabel = "Go back",
        onAction = onBack,
    )
}
