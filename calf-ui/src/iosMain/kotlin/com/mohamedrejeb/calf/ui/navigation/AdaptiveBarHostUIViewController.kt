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
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.removeFromParentViewController
import platform.UIKit.willMoveToParentViewController
import platform.darwin.dispatch_async
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
    BarHostContainer(configure, content).tabBarController

/**
 * The containers: a `UITabBarController` whose tabs are each a `UINavigationController` with one
 * thin [BarHostSlotController]. The single Compose view controller is a child of the selected
 * slot and moves with the selection; the tab bar is hidden while the shown screen has none.
 */
internal class BarHostContainer(
    configure: ComposeUIViewControllerConfiguration.() -> Unit,
    content: @Composable () -> Unit,
) {
    val tabBarController = UITabBarController()

    private var isApplyScheduled = false

    private val scrollMirror = LargeTitleScrollMirror(
        // The host already reports points.
        density = 1f,
        onSafeAreaInsetsChange = ::measure,
    )

    // A new screen or title style: let the mirror expand a large title again.
    private val driver = BarHostDriver(onTopBarLayoutChange = scrollMirror::recalibrate)

    private val composeViewController: UIViewController = ComposeUIViewController(configure) {
        HostContent(content)
    }

    val host = BarHost(
        contentViewController = composeViewController,
        onBarsChange = ::scheduleApply,
        onOwnerScrollChange = scrollMirror::reportScrollOffset,
    )

    private var slots: List<BarHostSlotController> = emptyList()
    private var navigationControllers: List<UINavigationController> = emptyList()
    private var selectedIndex = 0

    init {
        setTabCount(1)
        tabBarController.setTabBarVisibility(hidden = true)
        moveContentTo(0)
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

    /** Coalesces every bar change of one main-thread turn into one UIKit update. */
    private fun scheduleApply() {
        if (isApplyScheduled) return
        isApplyScheduled = true
        dispatch_async(dispatch_get_main_queue()) {
            isApplyScheduled = false
            driver.apply(host.state, navigationController, slot, host.density)
        }
    }

    private fun setTabCount(count: Int) {
        if (count == slots.size) return
        val newSlots = List(count) { index -> slots.getOrNull(index) ?: BarHostSlotController(::onSlotLayout) }
        navigationControllers = newSlots.mapIndexed { index, slot ->
            navigationControllers.getOrNull(index) ?: hostNavigationController(slot)
        }
        slots = newSlots
        tabBarController.setViewControllers(navigationControllers, animated = false)
    }

    /** Moves the Compose content into the slot of tab [index]; its composition stays alive. */
    private fun moveContentTo(index: Int) {
        val target = slots[index]
        selectedIndex = index
        if (composeViewController.parentViewController === target) return
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
        scrollMirror.install(target)
        target.setNeedsStatusBarAppearanceUpdate()
        scheduleApply()
    }

    private fun onSlotLayout(layoutSlot: BarHostSlotController) {
        if (layoutSlot !== slot) return
        scrollMirror.layout()
        measure()
    }

    /** The slot's safe area is what the containers' bars leave to the content. */
    private fun measure() {
        val insets = slot.view.safeAreaInsets.useContents {
            IosBarEdgeInsets(
                left = left.dp,
                // Not the live inset: it shrinks with the large title, see NavBarInsetCalibration.
                top = scrollMirror.contentTopInset(liveTopInset = top).dp,
                right = right.dp,
                bottom = bottom.dp,
            )
        }
        host.setLiveInsets(insets)
    }
}

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
private const val TAB_BAR_HIDING_MIN_IOS = 18
