@file:OptIn(BetaInteropApi::class)

package com.mohamedrejeb.calf.ui.fab

import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove
import com.mohamedrejeb.calf.ui.utils.toUIImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGAffineTransformMakeRotation
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSLog
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIImageSymbolConfiguration
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleLeftMargin
import platform.UIKit.UIViewAutoresizingFlexibleTopMargin
import platform.darwin.NSObject
import platform.darwin.sel_registerName
import kotlin.math.PI

private const val FAB_SIZE = 50.0
private const val FAB_MARGIN = 16.0
private const val ITEM_SPACING = 12.0

/**
 * Manages the native iOS expandable FAB view hierarchy with Liquid Glass effects.
 *
 * On iOS 26+, buttons use UIButtonConfiguration.glass() for native Liquid Glass styling.
 * The main FAB toggles expansion, revealing additional action buttons that animate
 * upward from the main FAB position.
 */
@OptIn(ExperimentalForeignApi::class)
internal class ExpandableFABManager(
    private val onExpandedChange: (Boolean) -> Unit,
    private val onItemSelected: (Int) -> Unit,
) {
    private var containerView: UIView? = null
    private var parentView: UIView? = null
    private var mainButton: UIButton? = null
    private var itemButtons: List<UIButton> = emptyList()
    private var isExpanded = false

    // Hold strong references to action handlers to prevent GC
    private var mainActionHandler: MainFABActionHandler? = null
    private var itemActionHandlers: List<ItemFABActionHandler> = emptyList()

    private val useGlass = isIOS26OrAbove()

    fun attachTo(view: UIView) {
        parentView = view

        val parentWidth: Double
        val parentHeight: Double
        view.frame.useContents {
            parentWidth = size.width
            parentHeight = size.height
        }
        NSLog("[ExpandableFAB] attachTo: parentView frame=${parentWidth}x${parentHeight}, useGlass=$useGlass")

        val container = UIView(
            frame = CGRectMake(0.0, 0.0, FAB_SIZE + FAB_MARGIN * 2, 400.0)
        ).apply {
            translatesAutoresizingMaskIntoConstraints = true
            autoresizingMask =
                UIViewAutoresizingFlexibleLeftMargin or UIViewAutoresizingFlexibleTopMargin
            clipsToBounds = false
        }

        containerView = container
        view.addSubview(container)

        updateContainerPosition()
    }

    fun detach() {
        NSLog("[ExpandableFAB] detach")
        containerView?.removeFromSuperview()
        containerView = null
        parentView = null
        mainButton = null
        itemButtons = emptyList()
        mainActionHandler = null
        itemActionHandlers = emptyList()
    }

    fun updateItems(
        mainImage: UIKitExpandableFABItem,
        items: List<UIKitExpandableFABItem>,
    ) {
        val container = containerView ?: run {
            NSLog("[ExpandableFAB] updateItems: containerView is null, skipping")
            return
        }
        NSLog("[ExpandableFAB] updateItems: mainImage=${mainImage.title}, items count=${items.size}")

        // Remove existing buttons
        mainButton?.removeFromSuperview()
        itemButtons.forEach { it.removeFromSuperview() }

        // Create action handlers
        mainActionHandler = MainFABActionHandler(this)
        itemActionHandlers = items.indices.map { index ->
            ItemFABActionHandler(this, index)
        }

        // Create item buttons (hidden initially, scaled down)
        val newItemButtons = items.mapIndexed { index, item ->
            createItemButton(item, index).apply {
                alpha = 0.0
                setTransform(CGAffineTransformMakeScale(0.3, 0.3))
                container.addSubview(this)
            }
        }
        itemButtons = newItemButtons

        // Create main button
        val newMainButton = createMainButton(mainImage)
        container.addSubview(newMainButton)
        mainButton = newMainButton

        layoutButtons()

        NSLog("[ExpandableFAB] updateItems: created ${items.size} item buttons + main button")
    }

    fun setExpanded(expanded: Boolean) {
        NSLog("[ExpandableFAB] setExpanded: $isExpanded -> $expanded")
        if (isExpanded == expanded) return
        isExpanded = expanded
        animateExpansion()
    }

    internal fun handleMainButtonTap() {
        NSLog("[ExpandableFAB] handleMainButtonTap: isExpanded=$isExpanded")
        onExpandedChange(!isExpanded)
    }

    internal fun handleItemButtonTap(index: Int) {
        NSLog("[ExpandableFAB] handleItemButtonTap: index=$index")
        onItemSelected(index)
        onExpandedChange(false)
    }

    private fun createMainButton(item: UIKitExpandableFABItem): UIButton {
        val symbolConfig = UIImageSymbolConfiguration.configurationWithPointSize(20.0)

        val config = if (useGlass) {
            UIButtonConfiguration.prominentGlassButtonConfiguration()
        } else {
            UIButtonConfiguration.filledButtonConfiguration()
        }.apply {
            setPreferredSymbolConfigurationForImage(symbolConfig)
            item.image?.toUIImage()?.let { setImage(it) }
        }

        val handler = mainActionHandler!!
        return UIButton.buttonWithConfiguration(config, primaryAction = null).apply {
            setFrame(CGRectMake(0.0, 0.0, FAB_SIZE, FAB_SIZE))
            addTarget(
                target = handler,
                action = sel_registerName("mainButtonTapped"),
                forControlEvents = UIControlEventTouchUpInside,
            )
        }
    }

    private fun createItemButton(item: UIKitExpandableFABItem, index: Int): UIButton {
        val symbolConfig = UIImageSymbolConfiguration.configurationWithPointSize(18.0)

        val config = if (useGlass) {
            UIButtonConfiguration.glassButtonConfiguration()
        } else {
            UIButtonConfiguration.filledButtonConfiguration()
        }.apply {
            setPreferredSymbolConfigurationForImage(symbolConfig)
            item.image?.toUIImage()?.let { setImage(it) }
        }

        val handler = itemActionHandlers[index]
        return UIButton.buttonWithConfiguration(config, primaryAction = null).apply {
            setFrame(CGRectMake(0.0, 0.0, FAB_SIZE, FAB_SIZE))
            tag = index.toLong()
            addTarget(
                target = handler,
                action = sel_registerName("itemButtonTapped"),
                forControlEvents = UIControlEventTouchUpInside,
            )
        }
    }

    private fun layoutButtons() {
        val container = containerView ?: return
        val main = mainButton ?: return

        // Main button at the bottom, item buttons stacked above
        val containerHeight = FAB_SIZE + (itemButtons.size * (FAB_SIZE + ITEM_SPACING))
        container.setFrame(
            CGRectMake(0.0, 0.0, FAB_SIZE + FAB_MARGIN * 2, containerHeight + FAB_MARGIN)
        )

        val mainY = containerHeight - FAB_SIZE
        main.setFrame(
            CGRectMake(FAB_MARGIN, mainY, FAB_SIZE, FAB_SIZE)
        )
        NSLog("[ExpandableFAB] layoutButtons: mainButton frame=($FAB_MARGIN, $mainY, $FAB_SIZE, $FAB_SIZE)")

        // Item buttons stacked above the main button
        itemButtons.forEachIndexed { index, button ->
            val y = containerHeight - FAB_SIZE - ((index + 1) * (FAB_SIZE + ITEM_SPACING))
            button.setFrame(CGRectMake(FAB_MARGIN, y, FAB_SIZE, FAB_SIZE))
            NSLog("[ExpandableFAB] layoutButtons: itemButton[$index] frame=($FAB_MARGIN, $y, $FAB_SIZE, $FAB_SIZE)")
        }

        updateContainerPosition()
    }

    private fun updateContainerPosition() {
        val parent = parentView ?: return
        val container = containerView ?: return

        val parentWidth: Double
        val parentHeight: Double
        parent.frame.useContents {
            parentWidth = size.width
            parentHeight = size.height
        }

        val containerWidth: Double
        val containerHeight: Double
        container.frame.useContents {
            containerWidth = size.width
            containerHeight = size.height
        }

        val x = parentWidth - containerWidth
        val y = parentHeight - containerHeight - FAB_MARGIN - 80.0 // 80pt offset for tab bar
        container.setFrame(
            CGRectMake(x, y, containerWidth, containerHeight)
        )
        NSLog("[ExpandableFAB] updateContainerPosition: parent=${parentWidth}x${parentHeight}, container=${containerWidth}x${containerHeight}, position=($x, $y)")
    }

    private fun animateExpansion() {
        val mainBtn = mainButton ?: return
        NSLog("[ExpandableFAB] animateExpansion: isExpanded=$isExpanded, itemButtons count=${itemButtons.size}")

        UIView.animateWithDuration(
            duration = 0.35,
            delay = 0.0,
            usingSpringWithDamping = 0.8,
            initialSpringVelocity = 0.5,
            options = 0u,
            animations = {
                itemButtons.forEach { button ->
                    if (isExpanded) {
                        button.alpha = 1.0
                        button.setTransform(CGAffineTransformMakeScale(1.0, 1.0))
                    } else {
                        button.alpha = 0.0
                        button.setTransform(CGAffineTransformMakeScale(0.3, 0.3))
                    }
                }

                // Rotate main button 45 degrees when expanded
                if (isExpanded) {
                    mainBtn.setTransform(CGAffineTransformMakeRotation(PI / 4.0))
                } else {
                    mainBtn.setTransform(CGAffineTransformMakeScale(1.0, 1.0))
                }
            },
            completion = null,
        )
    }
}

/**
 * Action handler for the main FAB button tap.
 */
@OptIn(ExperimentalForeignApi::class)
internal class MainFABActionHandler(
    private val manager: ExpandableFABManager,
) : NSObject() {
    @Suppress("unused")
    @ObjCAction
    fun mainButtonTapped() {
        manager.handleMainButtonTap()
    }
}

/**
 * Action handler for an item FAB button tap.
 */
@OptIn(ExperimentalForeignApi::class)
internal class ItemFABActionHandler(
    private val manager: ExpandableFABManager,
    private val index: Int,
) : NSObject() {
    @Suppress("unused")
    @ObjCAction
    fun itemButtonTapped() {
        manager.handleItemButtonTap(index)
    }
}
