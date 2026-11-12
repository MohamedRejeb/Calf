package com.mohamedrejeb.calf.sample.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.ui.theme.IconTileSize
import com.mohamedrejeb.calf.sample.ui.theme.PageMaxWidth
import com.mohamedrejeb.calf.sample.ui.theme.Spacing

/**
 * The page every demo is built on: a scrolling column of sections on the grouped background,
 * centred and capped at [PageMaxWidth] so it reads well in a wide pane.
 *
 * @param contentPadding the insets of the navigation entry or scaffold the page is shown in.
 * @param containerColor the page background; transparent when something is drawn behind the page.
 */
@Composable
fun DemoPage(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(containerColor)
            .verticalScroll(scrollState),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier
                .padding(contentPadding)
                .widthIn(max = PageMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = Spacing.pageMargin)
                .padding(top = Spacing.l, bottom = Spacing.xxl),
            content = content,
        )
    }
}

/**
 * A titled group of a page. The [title] sits above the content like a grouped list header, the
 * optional [footer] below it in small print.
 */
@Composable
fun DemoSection(
    title: String,
    modifier: Modifier = Modifier,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(title)
        Spacer(Modifier.height(Spacing.s))
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            content = content,
        )
        if (footer != null) {
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.xs, end = Spacing.xs, top = Spacing.s),
            )
        }
    }
}

/** The header of a [DemoSection], also used on its own above lists. */
@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = Spacing.xs),
    )
}

/** A plain rounded card on the grouped background, with inner padding. */
@Composable
fun DemoCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Spacing.l),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(Spacing.m),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

/** A card of [DemoRow]s: no inner padding, rows separated by [DemoDivider]. */
@Composable
fun DemoListCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    DemoCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.Top,
        content = content,
    )
}

/**
 * The divider between two rows of a [DemoListCard]. [startIndent] lines it up with the row's
 * text when the rows have a leading icon.
 */
@Composable
fun DemoDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = Spacing.l,
) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = modifier.padding(start = startIndent),
    )
}

/** The divider indent for rows with an [IconTile]. */
val DividerIndentWithIcon: Dp = Spacing.l + IconTileSize + Spacing.m
