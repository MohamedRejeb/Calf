package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
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
import com.mohamedrejeb.calf.ui.slider.AdaptiveSlider

private const val USAGE = """
var volume by remember { mutableFloatStateOf(0.6f) }

AdaptiveSlider(
    value = volume,
    onValueChange = { volume = it },
    onValueChangeFinished = { saveVolume(volume) },
)

// Discrete: 12 to 24 pt in steps of 2
AdaptiveSlider(value = size, onValueChange = { size = it }, valueRange = 12f..24f, steps = 5)
"""

private const val INITIAL_BRIGHTNESS = 0.7f
private const val INITIAL_VOLUME = 0.6f
private const val INITIAL_TEXT_SIZE = 16f
private const val MIN_TEXT_SIZE = 12f
private const val MAX_TEXT_SIZE = 24f

/** Ticks between the ends of the text size range: 12, 14, ..., 24 pt. */
private const val TEXT_SIZE_STEPS = 5
private const val DISABLED_VALUE = 0.4f
private const val PERCENT = 100

/** Line height of the preview text, relative to its size. */
private const val LINE_HEIGHT_RATIO = 1.4f

@Composable
fun SliderScreen(navigateBack: () -> Unit) {
    var brightness by rememberSaveable { mutableFloatStateOf(INITIAL_BRIGHTNESS) }
    var volume by rememberSaveable { mutableFloatStateOf(INITIAL_VOLUME) }
    var savedVolume by rememberSaveable { mutableFloatStateOf(INITIAL_VOLUME) }
    var textSize by rememberSaveable { mutableFloatStateOf(INITIAL_TEXT_SIZE) }

    SampleScreenScaffold(title = Screen.Slider.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.Slider)
            DemoSection(
                title = "Display & sound",
                footer = "Text size snaps to seven sizes between 12 and 24 pt.",
            ) {
                DemoListCard {
                    SliderRow(
                        title = "Brightness",
                        value = brightness.asPercent(),
                        icon = Icons.Outlined.LightMode,
                        tint = TileAmber,
                    ) {
                        AdaptiveSlider(
                            value = brightness,
                            onValueChange = { brightness = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    DemoDivider(startIndent = DividerIndentWithIcon)
                    SliderRow(
                        title = "Volume",
                        value = volume.asPercent(),
                        icon = Icons.AutoMirrored.Outlined.VolumeUp,
                        tint = TileCoral,
                    ) {
                        AdaptiveSlider(
                            value = volume,
                            onValueChange = { volume = it },
                            onValueChangeFinished = { savedVolume = volume },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    DemoDivider(startIndent = DividerIndentWithIcon)
                    SliderRow(
                        title = "Text size",
                        value = "${textSize.toInt()} pt",
                        icon = Icons.Outlined.FormatSize,
                        tint = TileBlue,
                    ) {
                        AdaptiveSlider(
                            value = textSize,
                            onValueChange = { textSize = it },
                            valueRange = MIN_TEXT_SIZE..MAX_TEXT_SIZE,
                            steps = TEXT_SIZE_STEPS,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            PreviewSection(textSize = textSize, savedVolume = savedVolume)
            DemoSection(title = "States") {
                DemoCard {
                    Text(
                        text = "Disabled",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AdaptiveSlider(
                        value = DISABLED_VALUE,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun PreviewSection(textSize: Float, savedVolume: Float) {
    DemoSection(
        title = "Preview",
        footer = "Saved volume changes in onValueChangeFinished, when you let go of the slider.",
    ) {
        DemoCard(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                text = "Marta Silva",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Dinner moved to 8 pm. Can you bring the playlist and a bottle of vinho verde?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = textSize.sp,
                    lineHeight = (textSize * LINE_HEIGHT_RATIO).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        DemoCard {
            ValueRow(label = "Saved volume", value = savedVolume.asPercent())
        }
    }
}

/** One setting of the panel: an icon, a title and the live value, with its slider below. */
@Composable
private fun SliderRow(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    slider: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.l, end = Spacing.l, top = Spacing.m),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            IconTile(icon = icon, color = tint)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        slider()
    }
}

private fun Float.asPercent(): String = "${(this * PERCENT).toInt()}%"
