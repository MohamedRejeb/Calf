package com.mohamedrejeb.calf.sample.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.roundToInt

// The palette of the Navigation 3 list-detail demo: named colours, and what the detail derives
// from each one.

/** A named colour of the palette, with the values the detail shows. */
internal data class Nav3Color(val name: String, val color: Color) {
    /** "#E34234" */
    val hex: String get() = color.hex()

    /** "227, 66, 52" */
    val rgb: String get() = color.rgb()

    /** "4°, 75%, 55%" */
    val hsl: String get() = color.hsl()

    /** Whether text on this colour reads better in black than in white. */
    val prefersDarkText: Boolean get() = color.luminance() > LUMINANCE_THRESHOLD

    /** Three tints, the colour itself and three shades, from lightest to darkest. */
    val tintsAndShades: List<Color>
        get() = MixSteps.map { lerp(color, Color.White, it) } + color +
            MixSteps.reversed().map { lerp(color, Color.Black, it) }
}

/** The colour behind a Navigation 3 key; ids start at 1. */
internal fun nav3Color(id: Int): Nav3Color = Palette[(id - 1).mod(Palette.size)]

/** The ids of the palette's colours, as the Navigation 3 keys carry them. */
internal val nav3ColorIds: IntRange get() = 1..Palette.size

private val Palette = listOf(
    Nav3Color("Vermilion", Color(0xFFE34234)),
    Nav3Color("Coral", Color(0xFFF2786B)),
    Nav3Color("Tangerine", Color(0xFFF28C28)),
    Nav3Color("Saffron", Color(0xFFF4C430)),
    Nav3Color("Moss", Color(0xFF7A8B3A)),
    Nav3Color("Sage", Color(0xFF9CAF88)),
    Nav3Color("Jade", Color(0xFF00A86B)),
    Nav3Color("Teal", Color(0xFF008080)),
    Nav3Color("Lagoon", Color(0xFF1B9AAA)),
    Nav3Color("Cerulean", Color(0xFF2A7AB0)),
    Nav3Color("Cobalt", Color(0xFF0047AB)),
    Nav3Color("Ultramarine", Color(0xFF4166F5)),
    Nav3Color("Iris", Color(0xFF5A4FCF)),
    Nav3Color("Indigo", Color(0xFF4B0082)),
    Nav3Color("Lavender", Color(0xFFB57EDC)),
    Nav3Color("Orchid", Color(0xFFDA70D6)),
    Nav3Color("Raspberry", Color(0xFFC72C5B)),
    Nav3Color("Rose", Color(0xFFE8798F)),
    Nav3Color("Terracotta", Color(0xFFE2725B)),
    Nav3Color("Sienna", Color(0xFFA0522D)),
    Nav3Color("Umber", Color(0xFF635147)),
    Nav3Color("Sand", Color(0xFFC2B280)),
    Nav3Color("Slate", Color(0xFF708090)),
    Nav3Color("Charcoal", Color(0xFF36454F)),
)

private const val CHANNEL_MAX = 255
private const val RGB_MASK = 0xFFFFFF
private const val HEX_DIGITS = 6
private const val HEX_RADIX = 16
private const val DEGREES_PER_SEXTANT = 60f
private const val SEXTANTS = 6f
private const val PERCENT = 100
private const val LUMINANCE_THRESHOLD = 0.4f

/** The mix steps of the tints (towards white) and shades (towards black) the detail shows. */
private val MixSteps = listOf(0.6f, 0.4f, 0.2f)

private fun Color.hex(): String =
    "#" + (toArgb() and RGB_MASK).toString(HEX_RADIX).padStart(HEX_DIGITS, '0').uppercase()

private fun Color.rgb(): String =
    listOf(red, green, blue).joinToString(", ") { (it * CHANNEL_MAX).roundToInt().toString() }

private fun Color.hsl(): String {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min
    val lightness = (max + min) / 2
    val saturation = if (delta == 0f) 0f else delta / (1 - abs(2 * lightness - 1))
    val sextant = when {
        delta == 0f -> 0f
        max == red -> ((green - blue) / delta).mod(SEXTANTS)
        max == green -> (blue - red) / delta + 2
        else -> (red - green) / delta + 4
    }
    val hue = (sextant * DEGREES_PER_SEXTANT).roundToInt()
    return "$hue°, ${(saturation * PERCENT).roundToInt()}%, ${(lightness * PERCENT).roundToInt()}%"
}
