package com.mohamedrejeb.calf.ui.uikit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontWeight

/**
 * Represents a font that can be converted to a UIFont on iOS.
 *
 * Supports different font types:
 * - [System]: The iOS system font (San Francisco) at a size and weight.
 * - [Named]: A font installed in the app, looked up by its PostScript name.
 *
 * @property size The font size in points.
 * @property scalesWithDynamicType Whether the font follows the user's Dynamic Type setting
 *   (`UIFontMetrics`). When `false`, the font keeps its fixed [size].
 */
@Immutable
sealed interface UIKitFont {
    val size: Float
    val scalesWithDynamicType: Boolean

    /**
     * The iOS system font.
     *
     * @param size The font size in points.
     * @param weight The font weight. Maps to the closest `UIFont.Weight`.
     * @param scalesWithDynamicType See [UIKitFont.scalesWithDynamicType].
     */
    data class System(
        override val size: Float,
        val weight: FontWeight = FontWeight.Normal,
        override val scalesWithDynamicType: Boolean = false,
    ) : UIKitFont

    /**
     * A font available to UIKit by name.
     *
     * The font must be registered with the system: bundled through the `UIAppFonts`
     * Info.plist key, or registered at runtime with `CTFontManagerRegisterGraphicsFont` /
     * `CTFontManagerRegisterFontsForURL`. Fonts loaded only through Compose resources are not
     * visible to UIKit. When the name is unknown, the system font of the same [size] is used.
     *
     * @param name The PostScript name of the font (e.g., "Rubik-Medium").
     * @param size The font size in points.
     * @param scalesWithDynamicType See [UIKitFont.scalesWithDynamicType].
     */
    data class Named(
        val name: String,
        override val size: Float,
        override val scalesWithDynamicType: Boolean = false,
    ) : UIKitFont
}
