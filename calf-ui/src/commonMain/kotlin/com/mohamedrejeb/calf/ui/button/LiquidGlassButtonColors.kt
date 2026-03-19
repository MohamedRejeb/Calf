package com.mohamedrejeb.calf.ui.button

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Color configuration for Liquid Glass buttons.
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
