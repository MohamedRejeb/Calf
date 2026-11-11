@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.utils

import androidx.compose.ui.text.font.FontWeight
import com.mohamedrejeb.calf.ui.uikit.UIKitFont
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIFont
import platform.UIKit.UIFontWeightBlack
import platform.UIKit.UIFontWeightBold
import platform.UIKit.UIFontWeightHeavy
import platform.UIKit.UIFontWeightLight
import platform.UIKit.UIFontWeightMedium
import platform.UIKit.UIFontWeightRegular
import platform.UIKit.UIFontWeightSemibold
import platform.UIKit.UIFontWeightThin
import platform.UIKit.UIFontWeightUltraLight
import kotlin.test.Test
import kotlin.test.assertEquals

private const val FONT_SIZE = 14f
private const val BUNDLED_FONT_NAME = "Helvetica-Bold"
private const val UNKNOWN_FONT_NAME = "Calf-NoSuchFont"

/** [UIKitFont] to `UIFont`, against the fonts UIKit really has. Runs on the iOS simulator. */
class FontUtilsTest {
    private val pointSize = FONT_SIZE.toDouble()

    @Test
    fun `the system font keeps its size`() {
        val font = UIKitFont.System(size = FONT_SIZE).toUIFont()

        assertEquals(UIFont.systemFontOfSize(pointSize, UIFontWeightRegular), font)
        assertEquals(pointSize, font.pointSize)
    }

    @Test
    fun `every Compose font weight maps to its UIKit weight`() {
        val expected = mapOf(
            FontWeight.W100 to UIFontWeightUltraLight,
            FontWeight.W200 to UIFontWeightThin,
            FontWeight.W300 to UIFontWeightLight,
            FontWeight.W400 to UIFontWeightRegular,
            FontWeight.W500 to UIFontWeightMedium,
            FontWeight.W600 to UIFontWeightSemibold,
            FontWeight.W700 to UIFontWeightBold,
            FontWeight.W800 to UIFontWeightHeavy,
            FontWeight.W900 to UIFontWeightBlack,
        )

        expected.forEach { (weight, uiWeight) ->
            assertEquals(
                UIFont.systemFontOfSize(pointSize, uiWeight),
                UIKitFont.System(size = FONT_SIZE, weight = weight).toUIFont(),
                "weight ${weight.weight}",
            )
        }
    }

    @Test
    fun `a weight between two steps takes the nearer one`() {
        assertEquals(
            UIFont.systemFontOfSize(pointSize, UIFontWeightSemibold),
            UIKitFont.System(size = FONT_SIZE, weight = FontWeight(620)).toUIFont(),
        )
    }

    @Test
    fun `a named font is looked up by its PostScript name`() {
        val font = UIKitFont.Named(name = BUNDLED_FONT_NAME, size = FONT_SIZE).toUIFont()

        assertEquals(BUNDLED_FONT_NAME, font.fontName)
        assertEquals(pointSize, font.pointSize)
    }

    @Test
    fun `an unknown font name falls back to the system font of the same size`() {
        val font = UIKitFont.Named(name = UNKNOWN_FONT_NAME, size = FONT_SIZE).toUIFont()

        assertEquals(UIFont.systemFontOfSize(pointSize), font)
    }

    @Test
    fun `a font that follows Dynamic Type stays the same font`() {
        val font = UIKitFont.Named(name = BUNDLED_FONT_NAME, size = FONT_SIZE, scalesWithDynamicType = true).toUIFont()

        assertEquals(BUNDLED_FONT_NAME, font.fontName)
    }
}
