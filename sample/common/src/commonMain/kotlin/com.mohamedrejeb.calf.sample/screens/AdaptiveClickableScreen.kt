package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable

private const val USAGE = """
Row(
    modifier = Modifier.adaptiveClickable(
        shape = MaterialTheme.shapes.large,
        onLongClick = { select(file) },
        onDoubleClick = { star(file) },
        onClick = { open(file) },
    ),
) {
    Text(file.name)
}
"""

private val FileRowMinHeight = 60.dp
private val TrailingIconSize = 20.dp
private val AlbumIconSize = 28.dp

/** Albums are a soft wash of their colour, calmer than a solid block. */
private const val ALBUM_TINT_ALPHA = 0.16f

private data class DemoFile(val name: String, val details: String, val icon: ImageVector, val tint: Color)

private data class Album(val name: String, val photos: Int, val tint: Color)

private val Files = listOf(
    DemoFile("Lisbon itinerary.pdf", "2.4 MB · Sep 12", Icons.Outlined.PictureAsPdf, TileCoral),
    DemoFile("Alfama at dusk.heic", "3.1 MB · Sep 14", Icons.Outlined.Image, TileBlue),
    DemoFile("Trip budget.xlsx", "48 KB · Sep 10", Icons.Outlined.TableChart, TileTeal),
    DemoFile("Packing list.txt", "2 KB · Sep 9", Icons.Outlined.Description, TileIndigo),
)

private val Albums = listOf(
    Album("Lisbon", photos = 48, tint = TileAmber),
    Album("Sintra", photos = 21, tint = TileTeal),
    Album("Porto", photos = 36, tint = TileViolet),
)

/** What the page has seen so far. Replaced, never mutated, on each gesture. */
@Immutable
private data class GestureLog(
    val lastGesture: String = "None yet",
    val target: String = "-",
    val clicks: Int = 0,
    val longClicks: Int = 0,
    val doubleClicks: Int = 0,
) {
    fun click(target: String) = copy(lastGesture = "Click", target = target, clicks = clicks + 1)
    fun longClick(target: String) = copy(lastGesture = "Long click", target = target, longClicks = longClicks + 1)
    fun doubleClick(target: String) =
        copy(lastGesture = "Double click", target = target, doubleClicks = doubleClicks + 1)
}

@Composable
fun AdaptiveClickableScreen(navigateBack: () -> Unit) {
    var log by remember { mutableStateOf(GestureLog()) }
    var starred by remember { mutableStateOf(setOf(Files[0].name)) }
    var selected by remember { mutableStateOf(emptySet<String>()) }

    SampleScreenScaffold(title = Screen.AdaptiveClickable.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.AdaptiveClickable)
            DemoSection(
                title = "Files",
                footer = "Tap opens a file, double tap stars it, long press selects it.",
            ) {
                DemoListCard {
                    Files.forEachIndexed { index, file ->
                        if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                        FileRow(
                            file = file,
                            starred = file.name in starred,
                            selected = file.name in selected,
                            onClick = { log = log.click(file.name) },
                            onDoubleClick = {
                                starred = starred.toggle(file.name)
                                log = log.doubleClick(file.name)
                            },
                            onLongClick = {
                                selected = selected.toggle(file.name)
                                log = log.longClick(file.name)
                            },
                        )
                    }
                }
            }
            GestureLogSection(log)
            AlbumsSection(
                onClick = { log = log.click(it.name) },
                onDoubleClick = { log = log.doubleClick(it.name) },
                onLongClick = { log = log.longClick(it.name) },
            )
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun GestureLogSection(log: GestureLog) {
    DemoSection(title = "Last gesture") {
        DemoCard {
            ValueRow(label = "Gesture", value = log.lastGesture)
            ValueRow(label = "On", value = log.target)
            ValueRow(label = "Clicks", value = log.clicks.toString())
            ValueRow(label = "Long clicks", value = log.longClicks.toString())
            ValueRow(label = "Double clicks", value = log.doubleClicks.toString())
        }
    }
}

@Composable
private fun FileRow(
    file: DemoFile,
    starred: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        modifier = Modifier
            .fillMaxWidth()
            .adaptiveClickable(onLongClick = onLongClick, onDoubleClick = onDoubleClick, onClick = onClick)
            .background(background)
            .heightIn(min = FileRowMinHeight)
            .padding(horizontal = Spacing.l, vertical = Spacing.s),
    ) {
        IconTile(icon = file.icon, color = file.tint)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = file.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (starred) TrailingIcon(Icons.Filled.Star, "Starred", TileAmber)
        if (selected) TrailingIcon(Icons.Filled.CheckCircle, "Selected", MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TrailingIcon(icon: ImageVector, contentDescription: String, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(TrailingIconSize),
    )
}

@Composable
private fun AlbumsSection(
    onClick: (Album) -> Unit,
    onDoubleClick: (Album) -> Unit,
    onLongClick: (Album) -> Unit,
) {
    DemoSection(
        title = "Albums",
        footer = "The shape parameter clips the press feedback to the rounded corners of each album.",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Albums.forEach { album ->
                AlbumTile(
                    album = album,
                    onClick = { onClick(album) },
                    onDoubleClick = { onDoubleClick(album) },
                    onLongClick = { onLongClick(album) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AlbumTile(
    album: Album,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .adaptiveClickable(
                    shape = MaterialTheme.shapes.large,
                    onLongClick = onLongClick,
                    onDoubleClick = onDoubleClick,
                    onClick = onClick,
                )
                .background(album.tint.copy(alpha = ALBUM_TINT_ALPHA)),
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoLibrary,
                contentDescription = null,
                tint = album.tint,
                modifier = Modifier.size(AlbumIconSize),
            )
        }
        Column(modifier = Modifier.padding(horizontal = Spacing.xs)) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${album.photos} photos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Set<String>.toggle(item: String): Set<String> = if (item in this) this - item else this + item
