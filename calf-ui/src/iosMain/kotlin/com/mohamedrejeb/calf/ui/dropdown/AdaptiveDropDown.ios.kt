@file:OptIn(BetaInteropApi::class)

package com.mohamedrejeb.calf.ui.dropdown

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalLayoutDirection
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.utils.applyLayoutDirection
import com.mohamedrejeb.calf.ui.utils.toUIImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypePlain
import platform.UIKit.UIColor
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIDevice
import platform.UIKit.UIEvent
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIMenu
import platform.UIKit.UIMenuElement
import platform.UIKit.UIMenuElementAttributesDestructive
import platform.UIKit.UIMenuElementAttributesDisabled
import platform.UIKit.UIMenuOptionsDisplayInline
import platform.UIKit.touchesBegan
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@ExperimentalCalfUiApi
@Composable
actual fun BoxScope.AdaptiveDropDown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    iosItems: List<AdaptiveDropDownItem>,
    modifier: Modifier,
    offset: DpOffset,
    scrollState: ScrollState,
    properties: PopupProperties,
    shape: Shape,
    containerColor: Color,
    tonalElevation: Dp,
    shadowElevation: Dp,
    border: BorderStroke?,
    iosSections: List<AdaptiveDropDownSection>,
    materialContent: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val viewController = LocalUIViewController.current

    val menuDelegate = remember { DropDownMenuDelegate() }
    menuDelegate.items = iosItems
    menuDelegate.sections = iosSections
    menuDelegate.onDismissRequest = onDismissRequest
    menuDelegate.density = density.density

    var posX by remember { mutableStateOf(0.0) }
    var posY by remember { mutableStateOf(0.0) }
    var width by remember { mutableStateOf(0.0) }
    var height by remember { mutableStateOf(0.0) }

    // Add the hidden UIButton to the ViewController's view hierarchy
    DisposableEffect(viewController) {
        val button = UIButton.buttonWithType(UIButtonTypePlain)
        button.backgroundColor = UIColor.clearColor
        button.setTitle("", forState = UIControlStateNormal)
        button.showsMenuAsPrimaryAction = true
//        button.alpha = 0.01

        viewController.view.addSubview(button)
        viewController.view.insertSubview(button, atIndex = 0)

        menuDelegate.button = button

        onDispose {
            button.removeFromSuperview()
            menuDelegate.button = null
        }
    }

    LaunchedEffect(layoutDirection) {
        menuDelegate.button?.applyLayoutDirection(layoutDirection)
    }

    // Update button position and size based on compose layout
    LaunchedEffect(posX, posY, width, height) {
        menuDelegate.button?.setFrame(CGRectMake(posX, posY, width, height))
    }

    // Update the menu whenever items/sections change
    LaunchedEffect(iosItems, iosSections) {
        menuDelegate.button?.menu = menuDelegate.buildMenu()
    }

    // Handle programmatic show/dismiss
    LaunchedEffect(expanded) {
        val button = menuDelegate.button ?: return@LaunchedEffect
        if (expanded) {
            // Use the context menu interaction to programmatically present the menu
            if (isPerformPrimaryActionAvailable()) {
                button.performPrimaryAction()
            } else {
                val gestureRecognizers =
                    button.gestureRecognizers.orEmpty().filterIsInstance<UIGestureRecognizer>()
                val gesture = gestureRecognizers.firstOrNull {
                    it.`class`()?.toString()
                        ?.contains("UITouchDownGestureRecognizer", true) ?: false
                }
                gesture?.touchesBegan(emptySet<Nothing>(), UIEvent())
            }
            onDismissRequest()
        }
    }

    // Invisible compose box to track position
    Box(
        modifier = modifier
            .matchParentSize()
            .onPlaced {
                val position = it.positionInWindow()
                posX = position.x.toDouble() / density.density
                posY = position.y.toDouble() / density.density
            }
            .onSizeChanged {
                width = it.width.toDouble() / density.density
                height = it.height.toDouble() / density.density
            }
    )
}

@OptIn(ExperimentalForeignApi::class)
internal class DropDownMenuDelegate : NSObject() {
    var items: List<AdaptiveDropDownItem> = emptyList()
    var sections: List<AdaptiveDropDownSection> = emptyList()
    var onDismissRequest: () -> Unit = {}
    var button: UIButton? = null
    var density: Float = 1f

    fun buildMenu(): UIMenu {
        val allActions: List<UIMenuElement> = items.map { it.toUIAction(density) }
        val sectionMenus: List<UIMenuElement> = sections.map { section ->
            UIMenu.menuWithTitle(
                title = section.title,
                image = null,
                identifier = null,
                options = UIMenuOptionsDisplayInline,
                children = section.items.map { it.toUIAction(density) },
            )
        }
        return UIMenu.menuWithTitle(
            title = "",
            children = allActions + sectionMenus,
        )
    }
}

private fun AdaptiveDropDownItem.toUIAction(density: Float): UIAction {
    val image = iosIcon?.toUIImage(density)
    val action = UIAction.actionWithTitle(
        title = title,
        image = image,
        identifier = null,
    ) { _ ->
        onClick()
    }
    var attributes: ULong = 0u
    if (isDestructive) attributes = attributes or UIMenuElementAttributesDestructive
    if (isDisabled) attributes = attributes or UIMenuElementAttributesDisabled
    action.attributes = attributes
    return action
}

private fun isPerformPrimaryActionAvailable(): Boolean {
    val systemVersion = UIDevice.currentDevice.systemVersion
    val major = systemVersion.split(".").firstOrNull()?.toIntOrNull() ?: 0
    val minor = systemVersion.split(".").getOrNull(1)?.toIntOrNull() ?: 0
    return major >= 17 && minor >= 4
}
