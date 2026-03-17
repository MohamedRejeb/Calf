package com.mohamedrejeb.calf.ui.toggle

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A Liquid Glass-style switch/toggle that uses the Backdrop library for real glass effects
 * on supported architectures (iosArm64, iosSimulatorArm64), and falls back
 * to a pure Compose approximation on unsupported architectures (iosX64).
 *
 * Matches the reference implementation at:
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/kmp/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidToggle.kt
 *
 * @param checked Whether the switch is checked
 * @param onCheckedChange Callback when the switch state changes
 * @param modifier The modifier to apply to the switch
 * @param thumbContent Optional content to display inside the thumb
 * @param enabled Whether the switch is enabled
 * @param colors The colors for the switch
 * @param interactionSource The interaction source for this switch
 */
@Composable
expect fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: LiquidGlassSwitchColors = LiquidGlassSwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
)

/**
 * Colors for [LiquidGlassSwitch].
 */
@Immutable
class LiquidGlassSwitchColors internal constructor(
    val checkedTrackColor: Color,
    val uncheckedTrackColor: Color,
    val checkedThumbColor: Color,
    val uncheckedThumbColor: Color,
    val checkedTrackBorderColor: Color,
    val uncheckedTrackBorderColor: Color,
    val checkedThumbBorderColor: Color,
    val uncheckedThumbBorderColor: Color,
    val disabledCheckedTrackColor: Color,
    val disabledUncheckedTrackColor: Color,
    val disabledCheckedThumbColor: Color,
    val disabledUncheckedThumbColor: Color,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LiquidGlassSwitchColors) return false
        if (checkedTrackColor != other.checkedTrackColor) return false
        if (uncheckedTrackColor != other.uncheckedTrackColor) return false
        if (checkedThumbColor != other.checkedThumbColor) return false
        if (uncheckedThumbColor != other.uncheckedThumbColor) return false
        if (checkedTrackBorderColor != other.checkedTrackBorderColor) return false
        if (uncheckedTrackBorderColor != other.uncheckedTrackBorderColor) return false
        if (checkedThumbBorderColor != other.checkedThumbBorderColor) return false
        if (uncheckedThumbBorderColor != other.uncheckedThumbBorderColor) return false
        if (disabledCheckedTrackColor != other.disabledCheckedTrackColor) return false
        if (disabledUncheckedTrackColor != other.disabledUncheckedTrackColor) return false
        if (disabledCheckedThumbColor != other.disabledCheckedThumbColor) return false
        if (disabledUncheckedThumbColor != other.disabledUncheckedThumbColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = checkedTrackColor.hashCode()
        result = 31 * result + uncheckedTrackColor.hashCode()
        result = 31 * result + checkedThumbColor.hashCode()
        result = 31 * result + uncheckedThumbColor.hashCode()
        result = 31 * result + checkedTrackBorderColor.hashCode()
        result = 31 * result + uncheckedTrackBorderColor.hashCode()
        result = 31 * result + checkedThumbBorderColor.hashCode()
        result = 31 * result + uncheckedThumbBorderColor.hashCode()
        result = 31 * result + disabledCheckedTrackColor.hashCode()
        result = 31 * result + disabledUncheckedTrackColor.hashCode()
        result = 31 * result + disabledCheckedThumbColor.hashCode()
        result = 31 * result + disabledUncheckedThumbColor.hashCode()
        return result
    }
}

@Stable
internal fun LiquidGlassSwitchColors.trackColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedTrackColor else uncheckedTrackColor
    } else {
        if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
    }

@Stable
internal fun LiquidGlassSwitchColors.thumbColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedThumbColor else uncheckedThumbColor
    } else {
        if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
    }

@Stable
internal fun LiquidGlassSwitchColors.trackBorderColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedTrackBorderColor else uncheckedTrackBorderColor
    } else {
        Color.Transparent
    }

@Stable
internal fun LiquidGlassSwitchColors.thumbBorderColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedThumbBorderColor else uncheckedThumbBorderColor
    } else {
        Color.Transparent
    }

/**
 * Defaults for [LiquidGlassSwitch].
 */
object LiquidGlassSwitchDefaults {

    val TrackWidth = 64.dp
    val TrackHeight = 28.dp
    val TrackShape = RoundedCornerShape(50)
    val TrackPadding = 2.dp

    val ThumbWidth = 40.dp
    val ThumbHeight = 24.dp

    /** Drag distance for the thumb (track - thumb - padding*2) */
    val DragWidth = 20.dp

    const val DisabledAlpha = 0.4f

    /** iOS 26 green accent for checked state */
    private val CheckedTrackColor = Color(0xFF34C759)

    /** Glass-like translucent track for unchecked state */
    private val UncheckedTrackColor = Color(0x33787878)

    /** Glass-like translucent white thumb */
    private val ThumbColor = Color(0xCCFFFFFF)

    /** Subtle glass border */
    private val GlassBorder = Color(0x50FFFFFF)

    /** Checked track border with slight green tint */
    private val CheckedTrackBorder = Color(0x3034C759)

    /** Unchecked track border */
    private val UncheckedTrackBorder = Color(0x40FFFFFF)

    /** Thumb border highlight */
    private val ThumbBorder = Color(0x60FFFFFF)

    fun colors(
        checkedTrackColor: Color = CheckedTrackColor,
        uncheckedTrackColor: Color = UncheckedTrackColor,
        checkedThumbColor: Color = ThumbColor,
        uncheckedThumbColor: Color = ThumbColor,
        checkedTrackBorderColor: Color = CheckedTrackBorder,
        uncheckedTrackBorderColor: Color = UncheckedTrackBorder,
        checkedThumbBorderColor: Color = ThumbBorder,
        uncheckedThumbBorderColor: Color = ThumbBorder,
        disabledCheckedTrackColor: Color = checkedTrackColor.copy(alpha = 0.3f),
        disabledUncheckedTrackColor: Color = uncheckedTrackColor.copy(alpha = 0.3f),
        disabledCheckedThumbColor: Color = checkedThumbColor.copy(alpha = 0.5f),
        disabledUncheckedThumbColor: Color = uncheckedThumbColor.copy(alpha = 0.5f),
    ): LiquidGlassSwitchColors = LiquidGlassSwitchColors(
        checkedTrackColor = checkedTrackColor,
        uncheckedTrackColor = uncheckedTrackColor,
        checkedThumbColor = checkedThumbColor,
        uncheckedThumbColor = uncheckedThumbColor,
        checkedTrackBorderColor = checkedTrackBorderColor,
        uncheckedTrackBorderColor = uncheckedTrackBorderColor,
        checkedThumbBorderColor = checkedThumbBorderColor,
        uncheckedThumbBorderColor = uncheckedThumbBorderColor,
        disabledCheckedTrackColor = disabledCheckedTrackColor,
        disabledUncheckedTrackColor = disabledUncheckedTrackColor,
        disabledCheckedThumbColor = disabledCheckedThumbColor,
        disabledUncheckedThumbColor = disabledUncheckedThumbColor,
    )
}
