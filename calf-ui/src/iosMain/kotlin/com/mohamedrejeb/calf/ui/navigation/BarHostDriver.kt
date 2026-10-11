package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.graphics.isSpecified
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UINavigationController
import platform.UIKit.UINavigationItem
import platform.UIKit.UINavigationItemLargeTitleDisplayMode
import platform.UIKit.UIView
import platform.UIKit.UIViewAnimationOptionTransitionCrossDissolve
import platform.UIKit.UIViewController
import platform.UIKit.navigationItem
import platform.UIKit.setToolbarItems

/**
 * Applies a [BarHostState]'s shown bars to the host's navigation controller and its content view
 * controller, touching UIKit only where something changed.
 *
 * When the owner changes (a screen was pushed or popped in Compose) the whole bar crossfades,
 * the way UIKit crossfades a pushed controller's title and items; Compose runs its own
 * transition underneath. The swap cannot follow the transition's progress: a navigation item
 * has no interactive transition of its own.
 */
internal class BarHostDriver(
    private val onTopBarLayoutChange: (owner: Any?) -> Unit,
) {

    private var applied: HostedBars? = null
    private var appliedOwner: Any? = null

    // Retained: UIKit holds bar-button targets weakly.
    private var leadingBinding = UIBarButtonItemsBinding.Empty
    private var trailingBinding = UIBarButtonItemsBinding.Empty
    private var toolbarBinding = UIBarButtonItemsBinding.Empty

    /** Shows [state]'s bars in [navigationController], whose content controller is [slot]. */
    fun apply(
        state: BarHostState,
        navigationController: UINavigationController,
        slot: UIViewController,
        density: Float,
    ) {
        if (state.owner == null) {
            clear(navigationController, slot)
            return
        }
        val bars = state.shownBars
        val isFirstApply = applied == null
        val ownerChanged = !isFirstApply && state.owner !== appliedOwner
        val previous = applied ?: HostedBars()
        if (!isFirstApply && bars == previous && !ownerChanged) return

        val change = {
            applyTopBar(previous.topBar, bars.topBar, navigationController, slot.navigationItem, density, isFirstApply)
            applyToolbar(previous.toolbarItems, bars.toolbarItems, slot, density, isFirstApply)
        }
        if (ownerChanged) {
            UIView.transitionWithView(
                view = navigationController.navigationBar,
                duration = OWNER_CROSSFADE_SECONDS,
                options = UIViewAnimationOptionTransitionCrossDissolve,
                animations = change,
                completion = null,
            )
        } else {
            change()
        }
        setBarsHidden(navigationController, bars, animated = !isFirstApply)
        if (topBarNeedsRecalibration(previous.topBar, bars.topBar, isNewLayout = isFirstApply || ownerChanged)) {
            onTopBarLayoutChange(state.owner)
        }

        applied = bars
        appliedOwner = state.owner
    }

    /**
     * No hosted scaffold is left (the app shows screens that do not use the host): the containers go
     * back to how a fresh host has them, bars hidden and empty, and nothing of the last screen is kept.
     */
    private fun clear(navigationController: UINavigationController, slot: UIViewController) {
        if (applied == null && appliedOwner == null) return
        val item = slot.navigationItem
        item.title = null
        item.setLeftBarButtonItems(null, animated = false)
        item.setRightBarButtonItems(null, animated = false)
        item.applyTitleAppearance(UIKitNavigationBarConfiguration())
        slot.setToolbarItems(null, animated = false)
        leadingBinding = UIBarButtonItemsBinding.Empty
        trailingBinding = UIBarButtonItemsBinding.Empty
        toolbarBinding = UIBarButtonItemsBinding.Empty
        setBarsHidden(navigationController, HostedBars(), animated = false)
        if (applied?.topBar != null) onTopBarLayoutChange(null)
        applied = null
        appliedOwner = null
    }

    private fun applyTopBar(
        old: HostedTopBar?,
        new: HostedTopBar?,
        navigationController: UINavigationController,
        item: UINavigationItem,
        density: Float,
        force: Boolean,
    ) {
        if (new == null) return
        // A bar coming back after a screen without one: the item still holds the last bar's title
        // and items (a hidden bar is not cleared), and nothing of it may be kept, an empty item
        // list included. Comparing with "no bar" would read empty against empty as unchanged.
        val force = force || old == null
        if (force || old?.title != new.title) item.title = new.title
        if (force || old?.configuration != new.configuration) {
            item.largeTitleDisplayMode =
                if (new.configuration.prefersLargeTitles)
                    UINavigationItemLargeTitleDisplayMode.UINavigationItemLargeTitleDisplayModeAlways
                else
                    UINavigationItemLargeTitleDisplayMode.UINavigationItemLargeTitleDisplayModeNever
            item.applyTitleAppearance(new.configuration)
            navigationController.navigationBar.translucent = new.configuration.isTranslucent
        }
        leadingBinding = updateItems(leadingBinding, old?.leadingItems, new.leadingItems, density, force) {
            item.setLeftBarButtonItems(it, animated = false)
        }
        trailingBinding = updateItems(trailingBinding, old?.trailingItems, new.trailingItems, density, force) {
            item.setRightBarButtonItems(it, animated = false)
        }
    }

    private fun applyToolbar(
        old: List<UIKitUIBarButtonItem>?,
        new: List<UIKitUIBarButtonItem>?,
        slot: UIViewController,
        density: Float,
        force: Boolean,
    ) {
        if (new == null) return
        toolbarBinding = updateItems(toolbarBinding, old, new, density, force) {
            slot.setToolbarItems(it, animated = false)
        }
    }

    private fun setBarsHidden(navigationController: UINavigationController, bars: HostedBars, animated: Boolean) {
        val hideNavigationBar = bars.topBar == null
        if (navigationController.navigationBarHidden != hideNavigationBar) {
            navigationController.setNavigationBarHidden(hideNavigationBar, animated)
        }
        val hideToolbar = bars.toolbarItems.isNullOrEmpty()
        if (navigationController.toolbarHidden != hideToolbar) {
            navigationController.setToolbarHidden(hideToolbar, animated)
        }
    }

    private fun updateItems(
        binding: UIBarButtonItemsBinding,
        old: List<UIKitUIBarButtonItem>?,
        new: List<UIKitUIBarButtonItem>,
        density: Float,
        force: Boolean,
        install: (List<*>) -> Unit,
    ): UIBarButtonItemsBinding =
        when (if (force) BarItemsUpdate.Replace else barItemsUpdate(old, new)) {
            BarItemsUpdate.None -> binding
            BarItemsUpdate.Rebind -> binding.also { it.rebind(new, density) }
            BarItemsUpdate.Replace -> new.toUIBarButtonItems(density).also { install(it.items) }
        }
}

/**
 * Per-item title colour, which UIKit carries through the crossfade (unlike the bar's own
 * appearance). An unspecified colour keeps the system appearance, and with it the transparent
 * scroll-edge look large titles rely on. Shared with calf-navigation's entries.
 */
@InternalCalfApi
public fun UINavigationItem.applyTitleAppearance(configuration: UIKitNavigationBarConfiguration) {
    if (!configuration.titleColor.isSpecified) {
        standardAppearance = null
        compactAppearance = null
        scrollEdgeAppearance = null
        return
    }
    val standard = configuration.toNavigationBarAppearance(isLiquidGlassEnabled = isIOS26OrAbove())
    val scrollEdge = UINavigationBarAppearance().apply {
        configureWithTransparentBackground()
        titleTextAttributes = standard.titleTextAttributes
        largeTitleTextAttributes = standard.largeTitleTextAttributes
    }
    standardAppearance = standard
    compactAppearance = standard
    scrollEdgeAppearance = scrollEdge
}

private const val OWNER_CROSSFADE_SECONDS = 0.25
