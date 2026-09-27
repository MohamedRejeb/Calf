package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.EmptyState
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.SectionTitle
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.PageMaxWidth
import com.mohamedrejeb.calf.sample.ui.theme.Spacing

private const val USAGE = """
CupertinoIcon(
    iconCode = CupertinoIcons.heartFill,
    size = 24.dp,
    tint = MaterialTheme.colorScheme.primary,
    contentDescription = "Favourite",
)
"""

/** Grid cells are at least this wide; the grid fits as many columns as it can. */
private val CellMinWidth = 80.dp

/** Room left under the grid so its last row scrolls clear of the selected icon's bar. */
private val SelectionBarClearance = 152.dp

@Composable
fun CupertinoIconsCatalogScreen(navigateBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(ALL_ICON_CATEGORIES) }
    // The bar keeps its icon while it slides out, so selection is an icon plus a visibility.
    var barIcon by remember { mutableStateOf<IconEntry?>(null) }
    var barVisible by remember { mutableStateOf(false) }

    val icons = remember(query, category) { filterIcons(query = query, category = category) }

    SampleScreenScaffold(title = Screen.CupertinoIconsCatalog.title, navigateBack = navigateBack) { padding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = CellMinWidth),
                contentPadding = PaddingValues(
                    start = Spacing.pageMargin,
                    end = Spacing.pageMargin,
                    top = padding.calculateTopPadding() + Spacing.l,
                    bottom = padding.calculateBottomPadding() + Spacing.xxl +
                        if (barVisible) SelectionBarClearance else 0.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier
                    .widthIn(max = PageMaxWidth)
                    .fillMaxSize(),
            ) {
                catalogHeader(
                    query = query,
                    onQueryChange = { query = it },
                    category = category,
                    onCategoryChange = { category = it },
                    count = icons.size,
                )
                items(items = icons, key = { it.name }) { icon ->
                    IconCell(
                        icon = icon,
                        selected = barVisible && icon == barIcon,
                        onClick = {
                            barVisible = !(barVisible && icon == barIcon)
                            barIcon = icon
                        },
                    )
                }
                if (icons.isEmpty()) {
                    fullWidthItem(key = "empty") {
                        EmptyState(
                            icon = Icons.Outlined.SearchOff,
                            title = "No icons match “$query”",
                            message = "Try another name, or look in all categories.",
                        )
                    }
                }
                fullWidthItem(key = "usage") {
                    DemoSection(title = "Usage", modifier = Modifier.padding(top = Spacing.xl)) {
                        CodeSnippet(USAGE.trimIndent())
                    }
                }
            }
            AnimatedVisibility(
                visible = barVisible,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                barIcon?.let { icon ->
                    SelectedIconBar(
                        icon = icon,
                        bottomPadding = padding.calculateBottomPadding(),
                        onDismiss = { barVisible = false },
                    )
                }
            }
        }
    }
}

/** The page header, the search field and the categories, each spanning the whole grid. */
private fun LazyGridScope.catalogHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    count: Int,
) {
    fullWidthItem(key = "header") {
        DemoHeader(Screen.CupertinoIconsCatalog, modifier = Modifier.padding(bottom = Spacing.l))
    }
    fullWidthItem(key = "search") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            IconSearchField(query = query, onQueryChange = onQueryChange)
            CategoryChips(selected = category, onSelect = onCategoryChange)
        }
    }
    fullWidthItem(key = "count") {
        SectionTitle(
            title = if (count == 1) "1 icon" else "$count icons",
            modifier = Modifier.padding(top = Spacing.m, bottom = Spacing.xs),
        )
    }
}

/** A page part spanning the grid. Its [key] is prefixed so it never clashes with an icon's name. */
private fun LazyGridScope.fullWidthItem(key: String, content: @Composable () -> Unit) {
    item(key = "page:$key", span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun filterIcons(query: String, category: String): List<IconEntry> {
    val inCategory = if (category == ALL_ICON_CATEGORIES) {
        AllCupertinoIcons
    } else {
        AllCupertinoIcons.filter { it.category == category }
    }
    val words = query.trim()
    if (words.isEmpty()) return inCategory
    // Matches "heart fill" as well as "heart_fill" and "heartFill".
    val normalised = words.replace(' ', '_')
    return inCategory.filter {
        it.name.contains(normalised, ignoreCase = true) ||
            it.constantName.contains(words, ignoreCase = true)
    }
}
