package com.mohamedrejeb.calf.ui.navigation

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownSection

/** What a native bar has to do when its [UIKitUIBarButtonItem]s are published again. */
@InternalCalfApi
public enum class BarItemsUpdate {
    /** Nothing changed. */
    None,

    /** Only the callbacks are new: point the existing native items at them, no visible change. */
    Rebind,

    /** Something visible changed: build new native items. */
    Replace,
}

/**
 * Compares two publications of the same bar's items. Every recomposition of a screen builds new
 * `onClick` lambdas, so plain equality would rebuild (and, animated, crossfade) the native items
 * on every recomposition; only a visible difference should. Shared with calf-navigation's entries.
 */
@InternalCalfApi
public fun barItemsUpdate(
    old: List<UIKitUIBarButtonItem>?,
    new: List<UIKitUIBarButtonItem>?,
): BarItemsUpdate {
    val before = old.orEmpty()
    val after = new.orEmpty()
    return when {
        before == after -> BarItemsUpdate.None
        before.map { it.withoutCallbacks() } == after.map { it.withoutCallbacks() } -> BarItemsUpdate.Rebind
        else -> BarItemsUpdate.Replace
    }
}

private val NoCallback: () -> Unit = {}

private fun UIKitUIBarButtonItem.withoutCallbacks(): UIKitUIBarButtonItem =
    copy(
        onClick = NoCallback,
        menuItems = menuItems.map { it.withoutCallback() },
        menuSections = menuSections.map { it.withoutCallbacks() },
    )

private fun AdaptiveDropDownItem.withoutCallback(): AdaptiveDropDownItem =
    copy(onClick = NoCallback)

private fun AdaptiveDropDownSection.withoutCallbacks(): AdaptiveDropDownSection =
    copy(items = items.map { it.withoutCallback() })
