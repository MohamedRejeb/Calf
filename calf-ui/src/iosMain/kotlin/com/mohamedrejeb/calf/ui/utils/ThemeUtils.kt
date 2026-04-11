package com.mohamedrejeb.calf.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import platform.UIKit.UISemanticContentAttributeForceLeftToRight
import platform.UIKit.UISemanticContentAttributeForceRightToLeft
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.UIViewController

internal fun UIViewController.applyTheme(dark: Boolean) {
    overrideUserInterfaceStyle =
        if (dark)
            UIUserInterfaceStyle.UIUserInterfaceStyleDark
        else
            UIUserInterfaceStyle.UIUserInterfaceStyleLight
}

internal fun UIView.applyTheme(dark : Boolean){
    listOf(this, superview).forEach {
        it?.overrideUserInterfaceStyle =
            if (dark)
                UIUserInterfaceStyle.UIUserInterfaceStyleDark
            else
                UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }
}

internal fun UIView.applyLayoutDirection(layoutDirection: LayoutDirection) {
    semanticContentAttribute = when (layoutDirection) {
        LayoutDirection.Rtl -> UISemanticContentAttributeForceRightToLeft
        else -> UISemanticContentAttributeForceLeftToRight
    }
}

internal fun UIViewController.applyLayoutDirection(layoutDirection: LayoutDirection) {
    view.applyLayoutDirection(layoutDirection)
}

internal fun isDark(color: Color): Boolean {
    val r = color.red * 255
    val g = color.green * 255
    val b = color.blue * 255

    val luminance = 0.299 * r + 0.587 * g + 0.114 * b
    return luminance <= 128
}