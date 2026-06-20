@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonSystemItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIImage
import platform.UIKit.UINavigationItem
import platform.UIKit.UINavigationItemLargeTitleDisplayMode
import platform.darwin.NSObject
import platform.objc.sel_registerName

/**
 * Holds the strong references to bar-button action handlers so they aren't
 * garbage-collected while the parent [EntryHostingController] is alive.
 * Returned from [applyNavBarDataToItem] so the caller can keep the references.
 */
internal data class NavBarItemHandlers(
    val leading: List<BarButtonActionHandler>,
    val trailing: List<BarButtonActionHandler>,
)

/**
 * Configures the given [UINavigationItem] from the supplied [IosNavBarData].
 * The returned [NavBarItemHandlers] must be retained by the caller — UIKit
 * holds only weak references to bar-button targets, so without strong
 * retention the handlers are GC'd and clicks become no-ops.
 *
 * Call this in the controller's `init` block so `navigationItem` is fully
 * populated *before* `setViewControllers(_, animated:)` runs the push
 * animation — that's what makes the title cross-fade animate correctly.
 */
internal fun applyNavBarDataToItem(
    item: UINavigationItem,
    data: IosNavBarData,
    density: Float,
): NavBarItemHandlers {
    item.title = data.title

    // The parent bar keeps `prefersLargeTitles = true`; per-entry visibility is
    // driven entirely by this display mode, which is what lets UIKit animate the
    // large-title shrink between a large-title screen and a regular one.
    item.largeTitleDisplayMode = if (data.largeTitle || data.configuration.prefersLargeTitles) {
        UINavigationItemLargeTitleDisplayMode.UINavigationItemLargeTitleDisplayModeAlways
    } else {
        UINavigationItemLargeTitleDisplayMode.UINavigationItemLargeTitleDisplayModeNever
    }

    val leadingHandlers = data.leadingItems.map { BarButtonActionHandler(it.onClick) }
    item.leftBarButtonItems = data.leadingItems.mapIndexed { index, source ->
        source.toUIBarButtonItem(leadingHandlers[index], density)
    }

    val trailingHandlers = data.trailingItems.map { BarButtonActionHandler(it.onClick) }
    item.rightBarButtonItems = data.trailingItems.mapIndexed { index, source ->
        source.toUIBarButtonItem(trailingHandlers[index], density)
    }

    data.backTitle?.let {
        item.backBarButtonItem = UIBarButtonItem(
            title = it,
            style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
            target = null,
            action = null,
        )
    }

    return NavBarItemHandlers(leadingHandlers, trailingHandlers)
}

/**
 * Bar-button click handler that wraps a Kotlin lambda in an Objective-C
 * target/action selector. The class must be retained for as long as the
 * `UIBarButtonItem` that points to it.
 */
internal class BarButtonActionHandler(
    private val onClick: () -> Unit,
) : NSObject() {
    @Suppress("unused")
    @ObjCAction
    fun handleAction() {
        onClick()
    }
}

/**
 * Convert a [UIKitUIBarButtonItem] to a real [UIBarButtonItem]. Minimal
 * implementation supporting `title`, `image`, `systemItem`, and the
 * `FlexibleSpace` / `FixedSpace` system items.
 *
 * Menu/dropdown support is intentionally not in this prototype — if you need
 * it, use `AdaptiveTopBar` standalone for now and we'll consolidate the
 * conversion logic with `calf-ui`'s in a follow-up.
 */
private fun UIKitUIBarButtonItem.toUIBarButtonItem(
    actionHandler: BarButtonActionHandler,
    density: Float,
): UIBarButtonItem {
    val selector = sel_registerName("handleAction")
    val capturedSystemItem = systemItem

    if (capturedSystemItem == UIKitUIBarButtonSystemItem.FlexibleSpace) {
        return UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
            target = null,
            action = null,
        )
    }
    if (capturedSystemItem == UIKitUIBarButtonSystemItem.FixedSpace) {
        return UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFixedSpace,
            target = null,
            action = null,
        ).also {
            it.width = width ?: 16.0
        }
    }

    if (capturedSystemItem != null) {
        return UIBarButtonItem(
            barButtonSystemItem = capturedSystemItem.toUIBarButtonSystemItem(),
            target = actionHandler,
            action = selector,
        ).also {
            it.enabled = enabled
            it.selected = selected
        }
    }

    val uiImage = image?.toUIImageOrNull(density)
    if (uiImage != null) {
        return UIBarButtonItem(
            image = uiImage,
            style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
            target = actionHandler,
            action = selector,
        ).also {
            it.enabled = enabled
            it.selected = selected
        }
    }

    return UIBarButtonItem(
        title = title ?: "",
        style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
        target = actionHandler,
        action = selector,
    ).also {
        it.enabled = enabled
        it.selected = selected
    }
}

private fun UIKitImage.toUIImageOrNull(@Suppress("UNUSED_PARAMETER") density: Float): UIImage? =
    when (this) {
        is UIKitImage.SystemName -> UIImage.systemImageNamed(name)
        // UIKitImage.Vector / UIKitImage.Bitmap need calf-ui's internal toUIImage(density)
        // which uses platform-specific rasterization. Not exposed cross-module today —
        // for the prototype, callers should pass SF Symbols (UIKitImage.SystemName).
        // Vector / Bitmap support will be added by exposing calf-ui's toUIImage helper
        // or duplicating the rasterization logic here.
        is UIKitImage.Vector,
        is UIKitImage.Bitmap -> null
    }

private fun UIKitUIBarButtonSystemItem.toUIBarButtonSystemItem(): UIBarButtonSystemItem =
    when (this) {
        UIKitUIBarButtonSystemItem.Done -> UIBarButtonSystemItem.UIBarButtonSystemItemDone
        UIKitUIBarButtonSystemItem.Cancel -> UIBarButtonSystemItem.UIBarButtonSystemItemCancel
        UIKitUIBarButtonSystemItem.Edit -> UIBarButtonSystemItem.UIBarButtonSystemItemEdit
        UIKitUIBarButtonSystemItem.Save -> UIBarButtonSystemItem.UIBarButtonSystemItemSave
        UIKitUIBarButtonSystemItem.Add -> UIBarButtonSystemItem.UIBarButtonSystemItemAdd
        UIKitUIBarButtonSystemItem.Compose -> UIBarButtonSystemItem.UIBarButtonSystemItemCompose
        UIKitUIBarButtonSystemItem.Reply -> UIBarButtonSystemItem.UIBarButtonSystemItemReply
        UIKitUIBarButtonSystemItem.Action -> UIBarButtonSystemItem.UIBarButtonSystemItemAction
        UIKitUIBarButtonSystemItem.Organize -> UIBarButtonSystemItem.UIBarButtonSystemItemOrganize
        UIKitUIBarButtonSystemItem.Trash -> UIBarButtonSystemItem.UIBarButtonSystemItemTrash
        UIKitUIBarButtonSystemItem.Search -> UIBarButtonSystemItem.UIBarButtonSystemItemSearch
        UIKitUIBarButtonSystemItem.Close -> UIBarButtonSystemItem.UIBarButtonSystemItemClose
        UIKitUIBarButtonSystemItem.FlexibleSpace -> UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace
        UIKitUIBarButtonSystemItem.FixedSpace -> UIBarButtonSystemItem.UIBarButtonSystemItemFixedSpace
    }

/**
 * Apply the [UIKitNavigationBarConfiguration] to the parent
 * `UINavigationController`'s `navigationBar`. Called from each wrapper VC's
 * `viewWillAppear` so the active entry's config wins as the user navigates.
 *
 * `prefersLargeTitles` is deliberately *not* set here: the bar-wide flag stays
 * `true` (set once by `IosNavController`) and per-entry large titles are
 * controlled via `navigationItem.largeTitleDisplayMode` in
 * [applyNavBarDataToItem] — toggling the bar-wide flag per entry would break
 * UIKit's shrink animation between large-title and regular screens.
 */
internal fun platform.UIKit.UINavigationBar.applyConfiguration(
    config: UIKitNavigationBarConfiguration,
) {
    translucent = config.isTranslucent
    // titleColor is intentionally not applied here in v1 — appearance config
    // is a parent-bar-wide concern. Per-VC tinting via UIKitNavigationBarConfiguration
    // can be added in a follow-up; for the prototype the bar uses default tint.
}
