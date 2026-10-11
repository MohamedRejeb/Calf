@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.NSDirectionalRectEdgeBottom
import platform.UIKit.NSDirectionalRectEdgeTop
import platform.UIKit.UIColor
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarControllerDelegateProtocol
import platform.UIKit.tabBarItem
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.removeFromParentViewController
import platform.UIKit.willMoveToParentViewController
import platform.darwin.NSObject
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_time
import platform.darwin.dispatch_get_main_queue
import platform.objc.sel_registerName

/**
 * The iOS entry point of an app whose screens use [AdaptiveScaffold] with plain Compose
 * navigation (Navigation 3, Navigation Compose, or your own): use it instead of
 * `ComposeUIViewController { }`.
 *
 * It hosts your Compose content in real UIKit containers, and the bars of the screen on top,
 * [AdaptiveTopBar], [AdaptiveToolbar] and [AdaptiveNavigationBar], become the containers' own
 * navigation bar, toolbar and tab bar. The bars stay in place while Compose transitions between
 * screens, and only their items change. That fixes bars sliding with the page during a
 * transition (#530), and on an iPhone Duo iOS moves them into the vertical bar next to the
 * content, which it only does for container-owned bars.
 *
 * Your navigation, transitions, shared elements and predictive back stay in Compose: there is
 * one Compose hierarchy, and the containers only show the bars. Without this entry point the
 * same components draw standalone UIKit bars, as before.
 *
 * ```swift
 * struct ComposeView: UIViewControllerRepresentable {
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         MainKt.MainViewController()
 *     }
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 * ```
 * ```kotlin
 * fun MainViewController(): UIViewController = AdaptiveBarHostUIViewController { App() }
 * ```
 *
 * The screen that composed last owns the bars; a screen with an [AdaptiveScaffold] but no top bar
 * hides it. Scaffolds inside a sheet, a dialog or a calf-navigation `AdaptiveNavDisplay` keep
 * drawing their own bars.
 *
 * @param configure configures the hosted `ComposeUIViewController`.
 * @param content the app's Compose content.
 */
@ExperimentalCalfUiApi
public fun AdaptiveBarHostUIViewController(
    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    content: @Composable () -> Unit,
): UIViewController =
    BarHostContainer(configure, content).rootController

/**
 * The containers: a `UINavigationController` whose one controller is a thin
 * [BarHostSlotController] holding the Compose view controller. Once a screen shows tabs, a
 * `UITabBarController` takes over with one such navigation controller per tab; the Compose view
 * controller moves into the selected tab's slot, and the tab bar is hidden while the shown
 * screen has none.
 *
 * The tab bar controller only exists once tabs were shown: a `UITabBarController` around the app
 * would take the vertical bar of an iPhone Duo from the tab bars of containers inside the Compose
 * content (a calf-navigation `AdaptiveTabDisplay`), even with its own tab bar hidden.
 */
internal class BarHostContainer(
    configure: ComposeUIViewControllerConfiguration.() -> Unit,
    content: @Composable () -> Unit,
) {
    val rootController = BarHostRootController()
    private var tabBarController: UITabBarController? = null

    private var isApplyScheduled = false
    private var isApplying = false

    private val scrollMirror = LargeTitleScrollMirror(
        // The host already reports points.
        density = 1f,
        onSafeAreaInsetsChange = ::measure,
    )

    // A new screen or title style: let the mirror expand a large title again, or bring back what
    // it had learnt for that screen (popping back to a scrolled list keeps its padding).
    private val driver = BarHostDriver(onTopBarLayoutChange = { owner -> scrollMirror.recalibrate(memoryKey = owner) })
    private val barColumn = BarHostBarColumn()

    private val composeViewController: UIViewController = ComposeUIViewController(configure) {
        HostContent(content)
    }

    val host = BarHost(
        contentViewController = composeViewController,
        onBarsChange = ::onBarsChange,
        onOwnerScrollChange = scrollMirror::reportScrollOffset,
    )

    private var slots: List<BarHostSlotController> = emptyList()
    private var navigationControllers: List<UINavigationController> = emptyList()
    private var selectedIndex = 0

    /** The outgoing tab's snapshot laid over a tab switch, see [freezeFrame]. */
    private var freezeSnapshot: UIView? = null

    private val tabBarDelegate = BarHostTabBarDelegate(::onTabTapped)
    private var appliedTabBar: HostedTabBar? = null
    private var hasAppliedBars = false

    // Restored when a tab bar configuration goes back to unspecified colours.
    private var defaultTintColor: UIColor? = null
    private var defaultUnselectedItemTintColor: UIColor? = null

    init {
        // The root is all the app keeps: it owns the container, and with it the host.
        rootController.container = this
        setTabCount(1)
        rootController.show(navigationControllers.first())
        moveContentTo(0)
    }

    /** The tab bar controller, created and put in place the first time a screen shows tabs. */
    private fun tabBarController(): UITabBarController =
        tabBarController ?: UITabBarController().also { controller ->
            tabBarController = controller
            controller.delegate = tabBarDelegate
            defaultTintColor = controller.tabBar.tintColor
            defaultUnselectedItemTintColor = controller.tabBar.unselectedItemTintColor
            // Out of the root first: a controller has one parent.
            rootController.show(controller)
            controller.setViewControllers(navigationControllers, animated = false)
        }

    @Composable
    private fun HostContent(content: @Composable () -> Unit) {
        val density = LocalDensity.current.density
        SideEffect { host.density = density }
        CompositionLocalProvider(LocalAdaptiveBarHost provides host) {
            ObserveBackGesture(host)
            content()
        }
    }

    private val navigationController: UINavigationController
        get() = navigationControllers[selectedIndex]

    private val slot: BarHostSlotController
        get() = slots[selectedIndex]

    /**
     * A change the containers must show now is applied right away: an entering screen publishes its
     * bars while Compose lays it out, and its content, composed next in the same pass, then gets
     * the insets of those bars on its first frame instead of the previous screen's. Everything is
     * applied again at the end of the main-thread turn, which also ends the generation.
     */
    private fun onBarsChange(immediate: Boolean) {
        if (immediate && hasAppliedBars) applyNow()
        scheduleApply()
    }

    /** Coalesces every bar change of one main-thread turn into one UIKit update. */
    private fun scheduleApply() {
        if (isApplyScheduled) return
        isApplyScheduled = true
        dispatch_async(dispatch_get_main_queue()) {
            isApplyScheduled = false
            applyNow()
            hasAppliedBars = true
            host.endGeneration()
        }
    }

    /** Shows the host's bars in the containers and measures what they leave to the content. */
    private fun applyNow() {
        // Applying lays the containers out, which measures, which can report a change again.
        if (isApplying) return
        isApplying = true
        try {
            val state = host.state
            // Tabs first: selecting another tab moves the content, and the bars follow it.
            applyTabBar(state.shownBars.tabBar, hasTabs = state.hasTabs, animated = hasAppliedBars)
            driver.apply(state, navigationController, slot, host.density)
            barColumn.apply(navigationController, slot, state.barColumnInsets())
            rootController.view.layoutIfNeeded()
            measure()
        } finally {
            // Whatever threw above, the next change must still reach UIKit.
            isApplying = false
        }
    }

    /**
     * Shows [tabBar] in the tab bar controller, one tab per item, the content in the selected
     * one; hides it when the shown screen has none. Hiding at the moment another screen takes the
     * bars (the start of its transition) is what keeps the tab bar from lingering over it.
     * Once no screen publishes tabs any more ([hasTabs]), the tab bar controller goes.
     */
    private fun applyTabBar(tabBar: HostedTabBar?, hasTabs: Boolean, animated: Boolean) {
        if (tabBar == null) {
            if (hasTabs) tabBarController?.setTabBarVisibility(hidden = true, animated = animated)
            else removeTabBarController()
            return
        }
        val tabBarController = tabBarController()
        val selected = tabBar.selectedIndex.coerceIn(tabBar.items.indices)
        // Items and configuration only: a selection change alone must not rebuild every tab bar
        // item while UIKit animates the selection.
        val itemsChanged = tabBar.items != appliedTabBar?.items || tabBar.configuration != appliedTabBar?.configuration
        if (itemsChanged) {
            // Fewer tabs: move the content out of a slot that is about to go before dropping it.
            if (selectedIndex >= tabBar.items.size) moveContentTo(selected)
            setTabCount(tabBar.items.size)
            tabBar.items.forEachIndexed { index, item ->
                navigationControllers[index].tabBarItem = item.toUITabBarItem(index.toLong(), host.density)
            }
            tabBarController.tabBar.applyTabBarConfiguration(
                configuration = tabBar.configuration,
                defaultTintColor = defaultTintColor ?: tabBarController.tabBar.tintColor,
                defaultUnselectedItemTintColor = defaultUnselectedItemTintColor,
            )
            appliedTabBar = tabBar
        }
        // UIKit crossfades the outgoing tab into the incoming one, and the re-parented Compose
        // view draws nothing for about 200 ms. Two snapshots of it bridge that: one stays in the
        // outgoing slot, so the tab fades out looking like itself (its bars are hidden later, see
        // BarHostDriver); one lies in the incoming slot above the Compose view, under that tab's
        // bars, and fades out once the view draws again. The screen's own transition runs
        // underneath, and the tab bar, outside the slots, animates its selection as usual.
        val switching = selected != selectedIndex && hasAppliedBars
        if (switching) leaveSnapshotBehind()
        val incoming = if (switching) composeViewController.view.snapshotViewAfterScreenUpdates(false) else null
        moveContentTo(selected)
        if (tabBarController.selectedIndex.toInt() != selected) {
            tabBarController.selectedIndex = selected.toULong()
        }
        incoming?.let { freezeFrame(it) }
        tabBarController.setTabBarVisibility(hidden = false, animated = animated)
    }

    /** A still of the content in the slot it is leaving; it goes when the content comes back. */
    private fun leaveSnapshotBehind() {
        val snapshot = composeViewController.view.snapshotViewAfterScreenUpdates(false) ?: return
        snapshot.tag = SNAPSHOT_TAG
        snapshot.setFrame(slot.view.bounds)
        snapshot.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        snapshot.userInteractionEnabled = false
        slot.view.addSubview(snapshot)
    }

    private fun freezeFrame(snapshot: UIView) {
        val slotView = slot.view
        freezeSnapshot?.removeFromSuperview()
        snapshot.tag = SNAPSHOT_TAG
        snapshot.setFrame(slotView.bounds)
        snapshot.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        snapshot.userInteractionEnabled = false
        slotView.addSubview(snapshot)
        freezeSnapshot = snapshot
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, FREEZE_FRAME_HOLD_NANOS), dispatch_get_main_queue()) {
            if (freezeSnapshot === snapshot) freezeSnapshot = null
            UIView.animateWithDuration(
                duration = FREEZE_FRAME_FADE_SECONDS,
                animations = { snapshot.alpha = 0.0 },
                completion = { snapshot.removeFromSuperview() },
            )
        }
    }

    /**
     * Back to the containers of a fresh host: one navigation controller as the root, a new one.
     * A `UITabBarController` around the app, even with its tab bar hidden, takes an iPhone Duo's
     * vertical bar from the tab bars of containers inside the Compose content (a calf-navigation
     * `AdaptiveTabDisplay`).
     *
     * The navigation controllers that were its tabs are dropped, not reused: once such a controller
     * is on its own again, UIKit no longer counts its navigation bar in its content's safe area
     * (the bar shows, the content's safe area stays at the status bar; iOS 26.2 and 26.3). The
     * content moves into a fresh slot in a fresh navigation controller, and the bars are applied
     * to it again from scratch.
     */
    private fun removeTabBarController() {
        val controller = tabBarController ?: return
        val freshSlot = BarHostSlotController(::onSlotLayout).also { it.loadViewIfNeeded() }
        val freshNavigationController = hostNavigationController(freshSlot)
        slots = listOf(freshSlot)
        navigationControllers = listOf(freshNavigationController)
        rootController.show(freshNavigationController)
        moveContentTo(0)
        controller.setViewControllers(emptyList<UIViewController>(), animated = false)
        controller.delegate = null
        tabBarController = null
        appliedTabBar = null
    }

    /**
     * A tab tapped in UIKit: the screen that shows the tabs decides, as on every other platform.
     * UIKit is told not to switch; once the screen publishes the new selection, [applyTabBar]
     * selects the tab and moves the content. Switching here first and letting the screen catch up
     * showed the previous tab's content for a frame or two when the screen was slow to recompose.
     */
    private fun onTabTapped(viewController: UIViewController): Boolean {
        val index = navigationControllers.indexOf(viewController)
        if (index < 0 || index == selectedIndex) return false
        host.onTabSelected(index)
        return false
    }

    private fun setTabCount(count: Int) {
        if (count == slots.size) return
        val newSlots = List(count) { index ->
            // Loaded now: the first visit of a tab took 0.3 s when the view loaded on selection.
            slots.getOrNull(index) ?: BarHostSlotController(::onSlotLayout).also { it.loadViewIfNeeded() }
        }
        navigationControllers = newSlots.mapIndexed { index, slot ->
            navigationControllers.getOrNull(index) ?: hostNavigationController(slot)
        }
        slots = newSlots
        tabBarController?.setViewControllers(navigationControllers, animated = false)
    }

    /** Moves the Compose content into the slot of tab [index]; its composition stays alive. */
    private fun moveContentTo(index: Int) {
        val target = slots[index]
        selectedIndex = index
        if (composeViewController.parentViewController === target) return
        // The still left behind when the content last left this slot.
        target.view.subviews.filterIsInstance<UIView>().filter { it.tag == SNAPSHOT_TAG }.forEach { it.removeFromSuperview() }
        composeViewController.willMoveToParentViewController(null)
        composeViewController.view.removeFromSuperview()
        composeViewController.removeFromParentViewController()

        target.addChildViewController(composeViewController)
        composeViewController.view.setFrame(target.view.bounds)
        composeViewController.view.autoresizingMask =
            UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        target.view.addSubview(composeViewController.view)
        composeViewController.didMoveToParentViewController(target)
        // Above the Compose view, see LargeTitleScrollMirror.
        scrollMirror.install(target, edges = NSDirectionalRectEdgeTop or NSDirectionalRectEdgeBottom)
        target.setNeedsStatusBarAppearanceUpdate()
        scheduleApply()
    }

    private fun onSlotLayout(layoutSlot: BarHostSlotController) {
        if (layoutSlot !== slot) return
        scrollMirror.layout()
        measure()
    }

    /**
     * The slot's safe area is what the containers' bars leave to the content; the root's is the
     * window's own, without them.
     */
    private fun measure() {
        val bars = slot.view.safeAreaInsets.useContents {
            IosBarEdgeInsets(
                left = left.dp,
                // Not the live inset: it shrinks with the large title, see NavBarInsetCalibration.
                top = scrollMirror.contentTopInset(liveTopInset = top).dp,
                right = right.dp,
                bottom = bottom.dp,
            )
        }
        val system = rootController.view.safeAreaInsets.useContents {
            IosBarEdgeInsets(left = left.dp, top = top.dp, right = right.dp, bottom = bottom.dp)
        }
        host.setMeasuredInsets(bars, system)
    }
}

/** The room the shown bars leave beside the owner's column, none when they span the window. */
private fun BarHostState.barColumnInsets(): IosBarEdgeInsets =
    if (shownBars.topBar == null && shownBars.toolbarItems == null)
        IosBarEdgeInsets.Zero
    else
        ownerSpan?.barColumnInsets() ?: IosBarEdgeInsets.Zero

private fun hostNavigationController(slot: UIViewController): UINavigationController =
    UINavigationController(rootViewController = slot).apply {
        navigationBar.prefersLargeTitles = true
        setNavigationBarHidden(true, animated = false)
        setToolbarHidden(true, animated = false)
        // One controller per stack: UIKit never pops it, and back is Compose's (CMP's own
        // edge-swipe recogniser). Off, so they can never compete for the touch.
        interactivePopGestureRecognizer?.enabled = false
        contentPopGestureRecognizer()?.enabled = false
    }

/** `interactiveContentPopGestureRecognizer` (iOS 26): the full-width swipe back. */
private fun UINavigationController.contentPopGestureRecognizer(): UIGestureRecognizer? =
    if (isIOSVersionAtLeast(LIQUID_GLASS_MIN_IOS) && respondsToSelector(sel_registerName("interactiveContentPopGestureRecognizer")))
        interactiveContentPopGestureRecognizer
    else
        null

/** `setTabBarHidden(_:animated:)` is iOS 18+; earlier systems only hide the view. */
internal fun UITabBarController.setTabBarVisibility(hidden: Boolean, animated: Boolean = false) {
    if (isIOSVersionAtLeast(TAB_BAR_HIDING_MIN_IOS)) {
        if (isTabBarHidden() != hidden) setTabBarHidden(hidden, animated = animated)
    } else {
        tabBar.hidden = hidden
    }
}

/** The controller handed to the app: it shows one container, and swaps it when tabs appear. */
internal class BarHostRootController : UIViewController(nibName = null, bundle = null) {
    /** Kept alive for as long as the app holds this controller. */
    var container: BarHostContainer? = null

    private var current: UIViewController? = null

    fun show(container: UIViewController) {
        current?.let { previous ->
            previous.willMoveToParentViewController(null)
            previous.view.removeFromSuperview()
            previous.removeFromParentViewController()
        }
        addChildViewController(container)
        container.view.setFrame(view.bounds)
        container.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        view.addSubview(container.view)
        container.didMoveToParentViewController(this)
        current = container
    }
}

/** Hands UIKit tab taps to the container before UIKit switches the tab. */
private class BarHostTabBarDelegate(
    private val onTabTapped: (UIViewController) -> Boolean,
) : NSObject(), UITabBarControllerDelegateProtocol {
    override fun tabBarController(
        tabBarController: UITabBarController,
        shouldSelectViewController: UIViewController,
    ): Boolean = onTabTapped(shouldSelectViewController)
}

/**
 * The thin content controller of one tab: it holds the Compose view controller while its tab is
 * selected and reports its layout passes, which carry every safe-area change of the containers.
 */
internal class BarHostSlotController(
    private val onLayout: (BarHostSlotController) -> Unit,
) : UIViewController(nibName = null, bundle = null) {

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        onLayout(this)
    }
}

private const val LIQUID_GLASS_MIN_IOS = 26

/** How long the outgoing content's snapshot covers the moved Compose view before it fades. */
private const val FREEZE_FRAME_HOLD_NANOS = 220_000_000L
private const val FREEZE_FRAME_FADE_SECONDS = 0.12
private const val SNAPSHOT_TAG = 0x43414C46L
private const val TAB_BAR_HIDING_MIN_IOS = 18
