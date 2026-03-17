package com.mohamedrejeb.calf.ui.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A Liquid Glass button that uses the Backdrop library for real glass effects
 * on supported architectures (iosArm64, iosSimulatorArm64), and falls back
 * to [CupertinoButton] on unsupported architectures (iosX64).
 *
 * When a Backdrop is provided via [LocalBackdrop], the button uses drawBackdrop
 * with vibrancy, blur, and lens effects matching the reference implementation at:
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/master/catalog/src/main/java/com/kyant/backdrop/catalog/components/LiquidButton.kt
 *
 * When no Backdrop is available, falls back to a translucent background approximation.
 *
 * @param onClick Called when the button is clicked
 * @param modifier The modifier to apply to the button
 * @param enabled Whether the button is enabled
 * @param colors The colors for the button
 * @param shape The shape of the button (Capsule by default, matching reference)
 * @param contentPadding The padding applied to the content
 * @param interactionSource The interaction source for this button
 * @param content The content of the button
 */
@Composable
expect fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: LiquidGlassButtonColors = LiquidGlassButtonDefaults.filledButtonColors(),
    shape: Shape = LiquidGlassButtonDefaults.Shape,
    contentPadding: PaddingValues = LiquidGlassButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
)

/**
 * Color configuration for [LiquidGlassButton].
 *
 * When a Backdrop is available, [tintColor] and [surfaceColor] are used with the
 * backdrop's `onDrawSurface` to render the glass tint (via `BlendMode.Hue`) and surface overlay.
 *
 * When no Backdrop is available, [fallbackContainerColor] is used as a translucent background.
 */
@Immutable
class LiquidGlassButtonColors(
    val tintColor: Color,
    val surfaceColor: Color,
    val contentColor: Color,
    val disabledContentColor: Color,
    val fallbackContainerColor: Color,
    val fallbackDisabledContainerColor: Color,
) {
    fun copy(
        tintColor: Color = this.tintColor,
        surfaceColor: Color = this.surfaceColor,
        contentColor: Color = this.contentColor,
        disabledContentColor: Color = this.disabledContentColor,
        fallbackContainerColor: Color = this.fallbackContainerColor,
        fallbackDisabledContainerColor: Color = this.fallbackDisabledContainerColor,
    ): LiquidGlassButtonColors = LiquidGlassButtonColors(
        tintColor = tintColor,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
        fallbackContainerColor = fallbackContainerColor,
        fallbackDisabledContainerColor = fallbackDisabledContainerColor,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LiquidGlassButtonColors) return false
        if (tintColor != other.tintColor) return false
        if (surfaceColor != other.surfaceColor) return false
        if (contentColor != other.contentColor) return false
        if (disabledContentColor != other.disabledContentColor) return false
        if (fallbackContainerColor != other.fallbackContainerColor) return false
        if (fallbackDisabledContainerColor != other.fallbackDisabledContainerColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = tintColor.hashCode()
        result = 31 * result + surfaceColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + disabledContentColor.hashCode()
        result = 31 * result + fallbackContainerColor.hashCode()
        result = 31 * result + fallbackDisabledContainerColor.hashCode()
        return result
    }
}

/**
 * Defaults for [LiquidGlassButton].
 *
 * Dimensions and behavior match the reference LiquidButton from AndroidLiquidGlass:
 * - Capsule shape
 * - 48dp height
 * - 16dp horizontal padding
 * - 8dp content spacing
 * - Scale-up press animation (~1.08x)
 * - drawBackdrop with vibrancy, blur(2dp), lens(12dp, 24dp) effects
 */
object LiquidGlassButtonDefaults {

    /** Capsule shape (fully rounded corners), matching reference Capsule() */
    val Shape: Shape = RoundedCornerShape(50)

    /** Fixed button height, matching reference .height(48f.dp) */
    val Height = 48.dp

    /** Default content padding, matching reference .padding(horizontal = 16f.dp) */
    val ContentPadding = PaddingValues(horizontal = 16.dp)

    /** Default content padding for plain (text) buttons */
    val PlainContentPadding = PaddingValues(horizontal = 8.dp)

    /** Icon button size */
    val IconSize = 48.dp

    /**
     * Scale factor when pressed.
     * Reference: lerp(1f, 1f + 4dp/height, progress) ≈ 1.083 for a 48dp button.
     */
    const val PressedScale = 1.08f

    /** Opacity when disabled */
    const val DisabledAlpha = 0.4f

    /** Glass-like translucent white for fallback */
    private val GlassWhite = Color(0x40FFFFFF)

    /** Disabled glass fallback */
    private val GlassDisabled = Color(0x20808080)

    /**
     * Filled liquid glass button colors.
     *
     * Pure glass with no tint — uses only vibrancy, blur, and lens effects.
     * Matching the default reference LiquidButton with no tint/surfaceColor.
     */
    fun filledButtonColors(
        tintColor: Color = Color.Unspecified,
        surfaceColor: Color = Color.Unspecified,
        contentColor: Color = Color.White,
        disabledContentColor: Color = Color.White.copy(alpha = 0.4f),
        fallbackContainerColor: Color = GlassWhite,
        fallbackDisabledContainerColor: Color = GlassDisabled,
    ): LiquidGlassButtonColors = LiquidGlassButtonColors(
        tintColor = tintColor,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
        fallbackContainerColor = fallbackContainerColor,
        fallbackDisabledContainerColor = fallbackDisabledContainerColor,
    )

    /**
     * Bordered/tinted liquid glass button colors.
     *
     * Applies a tint using `BlendMode.Hue`, matching the reference's tinted icon button pattern:
     * `drawRect(tint, blendMode = BlendMode.Hue); drawRect(tint.copy(alpha = 0.75f))`
     */
    fun borderedButtonColors(
        tintColor: Color = Color(0xFF007AFF),
        surfaceColor: Color = Color.Unspecified,
        contentColor: Color = Color.White,
        disabledContentColor: Color = Color.White.copy(alpha = 0.4f),
        fallbackContainerColor: Color = Color(0x18FFFFFF),
        fallbackDisabledContainerColor: Color = GlassDisabled,
    ): LiquidGlassButtonColors = LiquidGlassButtonColors(
        tintColor = tintColor,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
        fallbackContainerColor = fallbackContainerColor,
        fallbackDisabledContainerColor = fallbackDisabledContainerColor,
    )

    /**
     * Plain liquid glass button colors.
     * No glass background, content only, with subtle scale on press.
     */
    fun plainButtonColors(
        tintColor: Color = Color.Unspecified,
        surfaceColor: Color = Color.Unspecified,
        contentColor: Color = Color.White,
        disabledContentColor: Color = Color.White.copy(alpha = 0.4f),
        fallbackContainerColor: Color = Color.Transparent,
        fallbackDisabledContainerColor: Color = Color.Transparent,
    ): LiquidGlassButtonColors = LiquidGlassButtonColors(
        tintColor = tintColor,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
        fallbackContainerColor = fallbackContainerColor,
        fallbackDisabledContainerColor = fallbackDisabledContainerColor,
    )
}
