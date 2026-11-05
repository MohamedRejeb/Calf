package com.mohamedrejeb.calf.ui.utils

import androidx.compose.ui.text.font.FontWeight
import com.mohamedrejeb.calf.ui.uikit.UIKitFont
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIFont
import platform.UIKit.UIFontMetrics
import platform.UIKit.UIFontWeight
import platform.UIKit.UIFontWeightBlack
import platform.UIKit.UIFontWeightBold
import platform.UIKit.UIFontWeightHeavy
import platform.UIKit.UIFontWeightLight
import platform.UIKit.UIFontWeightMedium
import platform.UIKit.UIFontWeightRegular
import platform.UIKit.UIFontWeightSemibold
import platform.UIKit.UIFontWeightThin
import platform.UIKit.UIFontWeightUltraLight

/**
 * Converts a [UIKitFont] to a native iOS [UIFont].
 *
 * A [UIKitFont.Named] whose name UIKit does not know falls back to the system font of the
 * same size.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun UIKitFont.toUIFont(): UIFont {
    val pointSize = size.toDouble()
    val font = when (this) {
        is UIKitFont.System -> UIFont.systemFontOfSize(pointSize, weight.toUIFontWeight())
        is UIKitFont.Named -> UIFont.fontWithName(name, pointSize) ?: UIFont.systemFontOfSize(pointSize)
    }
    return if (scalesWithDynamicType) UIFontMetrics.defaultMetrics.scaledFontForFont(font) else font
}

@OptIn(ExperimentalForeignApi::class)
private fun FontWeight.toUIFontWeight(): UIFontWeight =
    when {
        weight < 150 -> UIFontWeightUltraLight
        weight < 250 -> UIFontWeightThin
        weight < 350 -> UIFontWeightLight
        weight < 450 -> UIFontWeightRegular
        weight < 550 -> UIFontWeightMedium
        weight < 650 -> UIFontWeightSemibold
        weight < 750 -> UIFontWeightBold
        weight < 850 -> UIFontWeightHeavy
        else -> UIFontWeightBlack
    }
