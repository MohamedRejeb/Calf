package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.EmptyState
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.button.AdaptiveButton

// Small building blocks shared by the navigation demos, on top of the sample's kit.

/** Size of the photo stand-in at the start of a list row. */
internal val NavDemoThumbnailSize = 44.dp

/** The divider indent for rows that start with a [NavDemoThumbnailSize] thumbnail or avatar. */
internal val NavDemoThumbnailIndent: Dp = Spacing.l + NavDemoThumbnailSize + Spacing.m

private val NavDemoHeroHeight = 200.dp
private val NavDemoHeroIconSize = 72.dp
private const val THUMBNAIL_ICON_FRACTION = 0.5f
private val NavDemoStatusMaxWidth = 420.dp
private val NavDemoInfoRowMinHeight = 52.dp

/** A coloured rounded square with a white icon: the stand-in for a photo in a list row. */
@Composable
internal fun NavDemoThumbnail(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = NavDemoThumbnailSize,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(color, MaterialTheme.shapes.medium),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * THUMBNAIL_ICON_FRACTION),
        )
    }
}

/** The large version of [NavDemoThumbnail] at the top of a detail page. */
@Composable
internal fun NavDemoHero(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(NavDemoHeroHeight)
            .background(color, MaterialTheme.shapes.large),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(NavDemoHeroIconSize),
        )
    }
}

/** A tip at the top of a screen: an icon tile, a short title and one or two sentences. */
@Composable
internal fun NavDemoHintCard(
    icon: ImageVector,
    tint: Color,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    DemoCard(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            IconTile(icon = icon, color = tint)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A whole screen that says one thing and offers one action: signed out, a locked form. Centred
 * in the space the entry leaves, on the grouped background.
 */
@Composable
internal fun NavDemoStatusPage(
    contentPadding: PaddingValues,
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            modifier = Modifier
                .widthIn(max = NavDemoStatusMaxWidth)
                .padding(horizontal = Spacing.pageMargin),
        ) {
            EmptyState(icon = icon, title = title, message = message)
            AdaptiveButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/**
 * A label and its value in a [DemoListCard] row, the value on the right: a fact about the content
 * ("Best time · Jun – Sep") or, in [monospace], a live readout. [action] sits after the value.
 * Label and value form one node, so a screen reader (and a test) reads them together.
 */
@Composable
internal fun NavDemoInfoRow(
    label: String,
    value: String,
    monospace: Boolean = false,
    valueMaxLines: Int = 1,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = NavDemoInfoRowMinHeight)
            .padding(horizontal = Spacing.l, vertical = Spacing.s),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {},
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = if (monospace) FontFamily.Monospace else null,
                color = if (monospace) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = valueMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        action?.invoke()
    }
}

/** A small tonal button at the end of a [NavDemoInfoRow]. */
@Composable
internal fun NavDemoRowButton(label: String, onClick: () -> Unit) {
    AdaptiveButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(),
        contentPadding = PaddingValues(horizontal = Spacing.l, vertical = Spacing.xs),
    ) {
        Text(label)
    }
}

/**
 * What has to survive a fold, a rotation or a window resize: the saved [taps] and, when the
 * entry has one, its ViewModel's [instance] number. The scroll position of the page around it is
 * the third check.
 */
@Composable
internal fun NavDemoStateCheck(
    taps: Int,
    onTap: () -> Unit,
    footer: String,
    instance: Int? = null,
) {
    DemoSection(title = "State check", footer = footer) {
        DemoListCard {
            if (instance != null) {
                NavDemoInfoRow(label = "ViewModel", value = "#$instance", monospace = true)
                DemoDivider()
            }
            NavDemoInfoRow(
                label = "Saved taps",
                value = taps.toString(),
                monospace = true,
                action = { NavDemoRowButton(label = "Tap", onClick = onTap) },
            )
        }
    }
}
