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
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSize
import platform.UIKit.NSDirectionalRectEdgeBottom
import platform.UIKit.NSDirectionalRectEdgeTop
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarControllerDelegateProtocol
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.UIViewControllerTransitionCoordinatorProtocol
import platform.UIKit.addChildViewController
import platform.UIKit.additionalSafeAreaInsets
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.removeFromParentViewController
import platform.UIKit.tabBarItem
import platform.UIKit.willMoveToParentViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.objc.sel_registerName
import kotlin.math.max

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
 * The containers: one `UINavigationController` whose one controller is a thin
 * [BarHostSlotController] holding the Compose view controller, for good. Once a screen shows
 * tabs, a `UITabBarController` takes the root with one empty [BarHostTabPlaceholderController]
 * per tab, and the navigation controller is layered over the tabs (see [layerContentOverTabs]);
 * the tab bar is hidden while the shown screen has none.
 *
 * The content never changes container on a tab switch. UIKit switches tabs by taking the
 * leaving tab's view out of the window and putting the entering one in, and Compose's view,
 * re-entering the window, takes first responder, lays out and draws a whole frame synchronously
 * with a wait for the GPU: tens of milliseconds on the main thread in the first frame of the tab
 * bar's selection animation, which stalled it. With the content in place, a switch is UIKit
 * animating its selection over two empty views while Compose transitions the content.
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

    private val slot = BarHostSlotController(::onSlotLayout)
    private val navigationController = hostNavigationController(slot)
    private var placeholders: List<BarHostTabPlaceholderController> = emptyList()
    private var selectedIndex = 0

    private val tabBarDelegate = BarHostTabBarDelegate(::onTabTapped)
    private var appliedTabBar: HostedTabBar? = null
    private var hasAppliedBars = false

    // Lives as long as the tab bar controller: it holds the looks that bar started with.
    private var tabBarConfigurationApplier: TabBarConfigurationApplier? = null

    init {
        // The root is all the app keeps: it owns the container, and with it the host.
        rootController.container = this
        slot.loadViewIfNeeded()
        slot.addChildViewController(composeViewController)
        composeViewController.view.setFrame(slot.view.bounds)
        composeViewController.view.autoresizingMask =
            UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        slot.view.addSubview(composeViewController.view)
        composeViewController.didMoveToParentViewController(slot)
        // Above the Compose view, see LargeTitleScrollMirror.
        scrollMirror.install(slot, edges = NSDirectionalRectEdgeTop or NSDirectionalRectEdgeBottom)
        rootController.show(navigationController)
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
     * Shows [tabBar] in the tab bar controller, one placeholder tab per item, the selected one
     * selected; hides it when the shown screen has none. Hiding at the moment another screen takes
     * the bars (the start of its transition) is what keeps the tab bar from lingering over it.
     * Once no screen publishes tabs any more ([hasTabs]), the tab bar controller goes.
     *
     * The selection is UIKit's own, animated as it animates a tap, and nothing else happens with
     * it: the content stays where it is and Compose transitions it.
     */
    private fun applyTabBar(tabBar: HostedTabBar?, hasTabs: Boolean, animated: Boolean) {
        if (tabBar == null) {
            if (hasTabs) tabBarController?.setTabBarVisibility(hidden = true, animated = animated)
            else removeTabBarController()
            return
        }
        val selected = tabBar.selectedIndex.coerceIn(tabBar.items.indices)
        // Items and configuration only: a selection change alone must not rebuild every tab bar
        // item while UIKit animates the selection.
        val itemsChanged = tabBar.items != appliedTabBar?.items || tabBar.configuration != appliedTabBar?.configuration
        if (itemsChanged) {
            setTabCount(tabBar.items.size)
            tabBar.items.forEachIndexed { index, item ->
                placeholders[index].tabBarItem = item.toUITabBarItem(index.toLong(), host.density)
            }
        }
        val tabBarController = tabBarController()
        if (itemsChanged) {
            tabBarConfigurationApplier?.apply(tabBar.configuration)
            appliedTabBar = tabBar
        }
        selectedIndex = selected
        if (tabBarController.selectedIndex.toInt() != selected) {
            tabBarController.selectedIndex = selected.toULong()
        }
        tabBarController.setTabBarVisibility(hidden = false, animated = animated)
        layerContentOverTabs()
    }

    /** The tab bar controller, created and put in place the first time a screen shows tabs. */
    private fun tabBarController(): UITabBarController =
        tabBarController ?: UITabBarController().also { controller ->
            tabBarController = controller
            controller.delegate = tabBarDelegate
            tabBarConfigurationApplier = TabBarConfigurationApplier(controller.tabBar)
            // The tab bar controller takes the root from the navigation controller, which goes over
            // its tabs in the same turn: out of the window for longer, Compose would drop its scene.
            rootController.show(controller)
            controller.setViewControllers(placeholders, animated = false)
            controller.view.layoutIfNeeded()
            layerContentOverTabs()
        }

    /**
     * Puts the content over the tabs: the navigation controller's view goes into the tab bar
     * controller's view above the container of the tabs' views and under the tab bar's own
     * container (which UIKit adds right above the tabs, after them), and the navigation
     * controller is on its own, with no parent. Checked on every apply: it costs an index lookup.
     *
     * It cannot be the tab bar controller's child: UIKit insists that a controller's view placed
     * inside another controller's view belongs to one of its children, and the tab bar controller
     * gives a child that is not the selected tab no safe area at all, its `additionalSafeAreaInsets`
     * included. On its own, the navigation controller takes the window's safe area through the
     * view hierarchy, gets its appearance callbacks and trait changes from UIKit all the same, and
     * only size transitions have to be forwarded to it, see [BarHostRootController]. The tab bar's
     * share of the safe area is mirrored from the selected placeholder, see [mirrorTabInsets].
     */
    private fun layerContentOverTabs() {
        val controller = tabBarController ?: return
        val contentView = navigationController.view
        val tabsContainer = controller.selectedViewController?.view?.ancestorDirectlyIn(controller.view)
        contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        if (tabsContainer == null) {
            // Before the tabs' views exist; the first placeholder layout puts it in order.
            if (contentView.superview != controller.view) {
                contentView.setFrame(controller.view.bounds)
                controller.view.addSubview(contentView)
            }
            return
        }
        val barContainer = controller.tabBar.ancestorDirectlyIn(controller.view)
        val subviews = controller.view.subviews
        val contentIndex = subviews.indexOf(contentView)
        val barIndex = barContainer?.let(subviews::indexOf) ?: subviews.size
        if (contentIndex > subviews.indexOf(tabsContainer) && contentIndex < barIndex) return
        contentView.setFrame(tabsContainer.frame)
        if (barContainer != null) {
            controller.view.insertSubview(contentView, belowSubview = barContainer)
        } else {
            controller.view.insertSubview(contentView, aboveSubview = tabsContainer)
        }
    }

    /**
     * Back to the containers of a fresh host: the navigation controller as the root, its safe
     * area the window's again. A `UITabBarController` around the app, even with its tab bar hidden,
     * takes an iPhone Duo's vertical bar from the tab bars of containers inside the Compose content
     * (a calf-navigation `AdaptiveTabDisplay`).
     */
    private fun removeTabBarController() {
        val controller = tabBarController ?: return
        navigationController.setAdditionalSafeAreaInsetsIfChanged(0.0, 0.0, 0.0, 0.0)
        rootController.show(navigationController)
        controller.setViewControllers(emptyList<UIViewController>(), animated = false)
        controller.delegate = null
        tabBarController = null
        tabBarConfigurationApplier = null
        placeholders = emptyList()
        appliedTabBar = null
    }

    /**
     * A tab tapped, or the selection dragged onto it (iOS 26), in UIKit. UIKit selects the empty tab
     * at once, so the indicator answers the touch as in any app, and the screen that shows the tabs
     * is told and decides, as on every other platform: it publishes the selection and the content
     * follows, or keeps the old one and the apply scheduled here puts the tab back. Refusing here and
     * selecting once the screen had confirmed took a dragged indicator back to the old item first.
     */
    private fun onTabTapped(viewController: UIViewController): Boolean {
        val index = placeholders.indexOf(viewController)
        if (index < 0 || index == selectedIndex) return false
        selectedIndex = index
        host.onTabSelected(index)
        scheduleApply()
        return true
    }

    private fun setTabCount(count: Int) {
        if (count == placeholders.size) return
        placeholders = List(count) { index ->
            placeholders.getOrNull(index) ?: BarHostTabPlaceholderController(::onPlaceholderLayout).also { placeholder ->
                // Loaded now: the first visit of a tab took 0.3 s when the view loaded on selection.
                placeholder.loadViewIfNeeded()
                // The tab bar follows the content's scroll view, which is in the other container: without
                // one for its edge UIKit draws it transparent over the content (iOS 15–18).
                scrollMirror.share(placeholder, edges = NSDirectionalRectEdgeBottom)
            }
        }
        tabBarController?.setViewControllers(placeholders, animated = false)
    }

    private fun onPlaceholderLayout(placeholder: BarHostTabPlaceholderController) {
        if (placeholder != tabBarController?.selectedViewController) return
        layerContentOverTabs()
        mirrorTabInsets(placeholder)
    }

    /**
     * The tab bar's share of the selected tab's safe area, what it has over the window's own, is
     * given to the content as additional insets; the window's own it gets from the view hierarchy.
     */
    private fun mirrorTabInsets(placeholder: UIViewController) {
        val system = rootController.view.safeAreaInsets
        placeholder.view.safeAreaInsets.useContents {
            val tab = this
            system.useContents {
                navigationController.setAdditionalSafeAreaInsetsIfChanged(
                    top = max(0.0, tab.top - top),
                    left = max(0.0, tab.left - left),
                    bottom = max(0.0, tab.bottom - bottom),
                    right = max(0.0, tab.right - right),
                )
            }
        }
    }

    /** The root rotates or resizes: the navigation controller on its own has to hear of it. */
    fun onSizeTransition(size: CValue<CGSize>, coordinator: UIViewControllerTransitionCoordinatorProtocol) {
        if (navigationController.parentViewController == null) {
            navigationController.viewWillTransitionToSize(size, coordinator)
        }
    }

    private fun onSlotLayout() {
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

/**
 * The ancestor of this view that is a direct subview of [root], or `null` when it is not inside it.
 * Compared with `equals` (`isEqual:`): Kotlin wraps a UIKit object anew each time it is handed one,
 * so identity never holds between two of them.
 */
private fun UIView.ancestorDirectlyIn(root: UIView): UIView? {
    var view: UIView? = this
    while (view != null && view.superview != root) view = view.superview
    return view
}

private fun UIViewController.setAdditionalSafeAreaInsetsIfChanged(top: Double, left: Double, bottom: Double, right: Double) {
    val unchanged = additionalSafeAreaInsets.useContents {
        this.top == top && this.left == left && this.bottom == bottom && this.right == right
    }
    if (!unchanged) additionalSafeAreaInsets = UIEdgeInsetsMake(top, left, bottom, right)
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

    override fun viewWillTransitionToSize(
        size: CValue<CGSize>,
        withTransitionCoordinator: UIViewControllerTransitionCoordinatorProtocol,
    ) {
        super.viewWillTransitionToSize(size, withTransitionCoordinator)
        container?.onSizeTransition(size, withTransitionCoordinator)
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
 * The content controller of the host's navigation controller: it holds the Compose view
 * controller and reports its layout passes, which carry every safe-area change of the containers.
 */
internal class BarHostSlotController(
    private val onLayout: () -> Unit,
) : UIViewController(nibName = null, bundle = null) {

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        onLayout()
    }
}

/**
 * One tab of the host's tab bar controller: an empty controller that gives UIKit something to
 * select, with the item of its tab. Invisible and touch-transparent, it lies under the content
 * (see [BarHostContainer.layerContentOverTabs]) and reports its layout passes, which carry the
 * tab bar's share of the safe area.
 */
internal class BarHostTabPlaceholderController(
    private val onLayout: (BarHostTabPlaceholderController) -> Unit,
) : UIViewController(nibName = null, bundle = null) {

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.clearColor
        view.userInteractionEnabled = false
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        onLayout(this)
    }
}

private const val LIQUID_GLASS_MIN_IOS = 26
private const val TAB_BAR_HIDING_MIN_IOS = 18
