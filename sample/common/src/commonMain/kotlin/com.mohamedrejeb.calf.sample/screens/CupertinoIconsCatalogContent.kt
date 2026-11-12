package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.cupertino.icons.CupertinoIcon
import com.mohamedrejeb.calf.sample.components.CodePill
import com.mohamedrejeb.calf.sample.ui.theme.PageMaxWidth
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable

/** The pill that shows every category. */
internal const val ALL_ICON_CATEGORIES = "All"
private const val CELL_NAME_LINES = 2
private val SearchFieldHeight = 44.dp
private val SmallIconSize = 20.dp
private val CellIconSize = 26.dp
private val PreviewSize = 64.dp
private val PreviewIconSize = 36.dp

/** The Kotlin constant of this icon in `CupertinoIcons`: `left_chevron` is `leftChevron`. */
internal val IconEntry.constantName: String
    get() = name.split('_').mapIndexed { index, part ->
        if (index == 0) part else part.replaceFirstChar { it.uppercaseChar() }
    }.joinToString("")

/** The name as words, so it wraps in a small grid cell. */
private val IconEntry.displayName: String
    get() = name.replace('_', ' ')

/** A rounded search field on the grouped background, with a clear button once there is text. */
@Composable
internal fun IconSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { field ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.shapes.medium)
                    .heightIn(min = SearchFieldHeight)
                    .padding(horizontal = Spacing.m),
            ) {
                SmallIcon(Icons.Outlined.Search, contentDescription = null)
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search icons",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    field()
                }
                if (query.isNotEmpty()) {
                    SmallIcon(
                        icon = Icons.Filled.Cancel,
                        contentDescription = "Clear search",
                        modifier = Modifier.adaptiveClickable(shape = CircleShape) { onQueryChange("") },
                    )
                }
            }
        },
    )
}

/** "All" and the catalogue's categories as a row of pills that scrolls sideways. */
@Composable
internal fun CategoryChips(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = modifier
            .bleedHorizontally(Spacing.pageMargin)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.pageMargin),
    ) {
        (listOf(ALL_ICON_CATEGORIES) + IconCategories).forEach { category ->
            CategoryChip(label = category, selected = category == selected, onClick = { onSelect(category) })
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .adaptiveClickable(shape = CircleShape, onClick = onClick)
            .background(if (selected) colors.primary else colors.surfaceContainerLowest)
            .padding(horizontal = Spacing.m + Spacing.xs, vertical = Spacing.s),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.onPrimary else colors.onSurface,
        )
    }
}

/** One icon of the grid: the glyph over its name, on a small card. */
@Composable
internal fun IconCell(
    icon: IconEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = modifier
            .adaptiveClickable(shape = MaterialTheme.shapes.medium, onClick = onClick)
            .background(if (selected) colors.primaryContainer else colors.surfaceContainerLowest)
            .padding(horizontal = Spacing.xs, vertical = Spacing.m),
    ) {
        CupertinoIcon(
            iconCode = icon.codepoint,
            size = CellIconSize,
            tint = if (selected) colors.onPrimaryContainer else colors.onSurface,
            contentDescription = icon.displayName,
        )
        Text(
            text = icon.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            minLines = CELL_NAME_LINES,
            maxLines = CELL_NAME_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * The tapped icon, large, with the call that draws it in code. Sits at the bottom of the screen
 * like a toolbar, above the scaffold's [bottomPadding].
 */
@Composable
internal fun SelectedIconBar(
    icon: IconEntry,
    bottomPadding: Dp,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            modifier = Modifier
                .widthIn(max = PageMaxWidth)
                .fillMaxWidth()
                .padding(Spacing.l),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.l),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(PreviewSize)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.medium),
                ) {
                    CupertinoIcon(
                        iconCode = icon.codepoint,
                        size = PreviewIconSize,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                SelectedIconDetails(icon = icon, modifier = Modifier.weight(1f))
                AdaptiveIconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
            // The whole call, on its own line so long constants fit.
            CodePill("CupertinoIcon(CupertinoIcons.${icon.constantName})")
        }
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
private fun SelectedIconDetails(icon: IconEntry, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = modifier,
    ) {
        Text(
            text = icon.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = icon.category,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SmallIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.size(SmallIconSize),
    )
}

/**
 * Lays the content out [margin] wider on both sides, so a row that scrolls sideways runs to the
 * edges of the grid instead of stopping at the page margin.
 */
private fun Modifier.bleedHorizontally(margin: Dp): Modifier = layout { measurable, constraints ->
    val extra = (margin * 2).roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.maxWidth + extra,
            maxWidth = constraints.maxWidth + extra,
        ),
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-margin.roundToPx(), 0)
    }
}
