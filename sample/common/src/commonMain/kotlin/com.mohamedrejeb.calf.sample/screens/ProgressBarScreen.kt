package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import kotlinx.coroutines.delay

private const val USAGE = """
if (isSyncing) {
    AdaptiveCircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = MaterialTheme.colorScheme.primary,
    )
} else {
    AdaptiveButton(onClick = { isSyncing = true }) { Text("Sync") }
}
"""

/** How long the fake sync runs. */
private const val SYNC_DURATION_MILLIS = 2_000L
private const val TRACK_ALPHA = 0.2f
private val RowIndicatorSize = 24.dp
private val RowIndicatorStroke = 3.dp
private val SyncSlotWidth = 84.dp
private val MediumSize = 60.dp
private val LargeSize = 90.dp
private val ThinStroke = 2.dp

/** Height of a slot in the sizes row, so the labels line up under indicators of different sizes. */
private val SampleSlotHeight = LargeSize

@Composable
fun ProgressBarScreen(navigateBack: () -> Unit) {
    SampleScreenScaffold(title = Screen.ProgressBar.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.ProgressBar)
            DemoSection(
                title = "Backup",
                footer = "Sync runs for two seconds and shows the indicator in place of the button.",
            ) {
                DemoListCard {
                    SyncRow(
                        title = "Photos",
                        idleStatus = "1,284 photos · Synced yesterday",
                        icon = Icons.Outlined.PhotoLibrary,
                        tint = TileBlue,
                    )
                    DemoDivider(startIndent = DividerIndentWithIcon)
                    SyncRow(
                        title = "Documents",
                        idleStatus = "312 files · Synced 2 hours ago",
                        icon = Icons.Outlined.Description,
                        tint = TileAmber,
                    )
                    DemoDivider(startIndent = DividerIndentWithIcon)
                    SyncRow(
                        title = "Contacts",
                        idleStatus = "248 contacts · Synced today",
                        icon = Icons.Outlined.Contacts,
                        tint = TileTeal,
                    )
                }
            }
            SizesSection()
            ColoursSection()
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun SyncRow(
    title: String,
    idleStatus: String,
    icon: ImageVector,
    tint: Color,
) {
    var syncing by rememberSaveable { mutableStateOf(false) }
    var justSynced by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(syncing) {
        if (syncing) {
            delay(SYNC_DURATION_MILLIS)
            syncing = false
            justSynced = true
        }
    }

    DemoRow(
        title = title,
        subtitle = when {
            syncing -> "Syncing…"
            justSynced -> "Up to date · Just now"
            else -> idleStatus
        },
        leading = { IconTile(icon = icon, color = tint) },
        trailing = {
            // A fixed slot, so the subtitle does not reflow when the button becomes a spinner.
            Box(contentAlignment = Alignment.Center, modifier = Modifier.widthIn(min = SyncSlotWidth)) {
                if (syncing) {
                    AdaptiveCircularProgressIndicator(
                        modifier = Modifier.size(RowIndicatorSize),
                        strokeWidth = RowIndicatorStroke,
                    )
                } else {
                    AdaptiveButton(onClick = { syncing = true }) { Text("Sync") }
                }
            }
        },
    )
}

@Composable
private fun SizesSection() {
    DemoSection(title = "Sizes") {
        DemoCard {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IndicatorSample(label = "Default") { AdaptiveCircularProgressIndicator() }
                IndicatorSample(label = "60 dp") {
                    AdaptiveCircularProgressIndicator(modifier = Modifier.size(MediumSize))
                }
                IndicatorSample(label = "90 dp") {
                    AdaptiveCircularProgressIndicator(modifier = Modifier.size(LargeSize))
                }
            }
        }
    }
}

@Composable
private fun ColoursSection() {
    val tertiary = MaterialTheme.colorScheme.tertiary
    DemoSection(
        title = "Colours",
        footer = "On iOS only the colour applies: the activity indicator has no track or stroke width.",
    ) {
        DemoCard {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IndicatorSample(label = "Primary", slotHeight = MediumSize) {
                    AdaptiveCircularProgressIndicator()
                }
                IndicatorSample(label = "Tertiary, track", slotHeight = MediumSize) {
                    AdaptiveCircularProgressIndicator(
                        color = tertiary,
                        trackColor = tertiary.copy(alpha = TRACK_ALPHA),
                    )
                }
                IndicatorSample(label = "Teal, thin", slotHeight = MediumSize) {
                    AdaptiveCircularProgressIndicator(color = TileTeal, strokeWidth = ThinStroke)
                }
            }
        }
    }
}

/** An indicator centred in a fixed-height slot with its [label] below. */
@Composable
private fun IndicatorSample(
    label: String,
    slotHeight: Dp = SampleSlotHeight,
    indicator: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(width = LargeSize, height = slotHeight)) {
            indicator()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
