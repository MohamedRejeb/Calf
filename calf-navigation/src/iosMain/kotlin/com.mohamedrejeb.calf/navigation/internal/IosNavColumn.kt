@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UIGestureRecognizerStateBegan
import platform.UIKit.UIGestureRecognizerStateChanged
import platform.UIKit.UINavigationController
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.transitionCoordinator
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import platform.objc.sel_registerName

/** Longer than UIKit's push and pop animation (about 0.35 s); a gesture keeps the watchdog waiting. */
private const val TRANSITION_WATCHDOG_NANOS = 1_000_000_000L

/**
 * One real [UINavigationController] and everything Calf attaches to it, whatever decides which
 * controllers it shows: [IosNavController] mirrors a whole back stack into one column,
 * [IosSplitController] spreads one back stack over three.
 *
 *  - **Swipe-back policy**: every pop recognizer the system has follows the top entry's
 *    `iosGestureEnabled` (see [canPopInteractively]).
 *  - **Transition tracking**: [isTransitioning] is true between this controller's own animated
 *    `willShow` and its `didShow`, including an interactive pop in flight. A `willShow` that is
 *    not animated is an appearance (the tab was selected, a split view column came on screen), not
 *    a transition UIKit could not interrupt: counting it dropped navigation requested together
 *    with a tab switch, which is what a deep link does. `transitionCoordinator` is not used: it
 *    also reports ancestors' transitions and stays non-nil inside `didShow`.
 *  - **Large titles**: the bar-wide flag stays on; each entry's `navigationItem` decides whether
 *    its own title is large.
 *
 * @param onDidShow called after every finished transition, which is when a native pop (back
 *   button, edge swipe, pop to root) has to be mirrored into the back stack.
 */
internal class IosNavColumn(
    private val onDidShow: () -> Unit,
) {
    /** Resolved once: the OS version cannot change while the app runs. */
    private val managedGestures: List<PopGestureKind> = managedPopGestures(::isIOSVersionAtLeast)
    private val gestureDelegate = PopGestureDelegate(::canPopTopEntry)
    private val navigationDelegate = NavigationDelegate(
        onWillShow = { animated -> if (animated) onUiKitWillShow() },
        onDidShow = ::onUiKitDidShow,
    )

    val uiNavigationController: UINavigationController = object : UINavigationController(
        nibName = null,
        bundle = null,
    ) {
        override fun viewDidLoad() {
            super.viewDidLoad()
            navigationBar.prefersLargeTitles = true
            refreshPopGesture()
        }
    }

    var isTransitioning: Boolean = false
        private set

    /** Identifies the transition a watchdog was armed for; a later transition re-arms it. */
    private var transitionGeneration: Int = 0

    /** What UIKit currently holds, bottom first. */
    val controllers: List<UIViewController>
        get() = uiNavigationController.viewControllers.filterIsInstance<UIViewController>()

    val topController: UIViewController?
        get() = uiNavigationController.topViewController

    init {
        uiNavigationController.delegate = navigationDelegate
    }

    /** Hands [controllers] to UIKit, which animates the push or pop when [animated]. */
    fun show(controllers: List<UIViewController>, animated: Boolean) {
        uiNavigationController.setViewControllers(controllers, animated = animated)
        refreshPopGesture()
    }

    /**
     * Forgets a transition that can no longer finish: a column taken off screen by a split view
     * collapse or expansion never gets its `didShow`, and would refuse navigation from then on.
     */
    fun resetTransition() {
        isTransitioning = false
        transitionGeneration++
    }

    private fun onUiKitWillShow() {
        isTransitioning = true
        armWatchdog(++transitionGeneration)
    }

    private fun onUiKitDidShow() {
        resetTransition()
        refreshPopGesture()
        onDidShow()
    }

    /**
     * A `didShow` can fail to come: the tab was switched or the app backgrounded while a push
     * animated, the controller left the window. [isTransitioning] would then stay true and every
     * later change would be deferred for good. The watchdog looks at the controller after UIKit's
     * push duration: while a pop gesture is in flight or a transition coordinator still exists it
     * waits again; otherwise it ends the transition the way `didShow` would have.
     */
    private fun armWatchdog(generation: Int) {
        dispatch_after(
            dispatch_time(DISPATCH_TIME_NOW, TRANSITION_WATCHDOG_NANOS),
            dispatch_get_main_queue(),
        ) {
            if (generation != transitionGeneration || !isTransitioning) return@dispatch_after
            if (isPopGestureInFlight() || uiNavigationController.transitionCoordinator != null) {
                armWatchdog(generation)
            } else {
                onUiKitDidShow()
            }
        }
    }

    private fun isPopGestureInFlight(): Boolean =
        listOfNotNull(
            uiNavigationController.interactivePopGestureRecognizer,
            uiNavigationController.contentPopGestureRecognizer(),
        ).any { it.state == UIGestureRecognizerStateBegan || it.state == UIGestureRecognizerStateChanged }

    private fun canPopTopEntry(): Boolean =
        canPopInteractively(
            stackSize = uiNavigationController.viewControllers.size,
            topGestureEnabled = (uiNavigationController.topViewController as? EntryHostingController)?.gestureEnabled,
        )

    /**
     * Re-asserts swipe-back configuration on every recognizer the system has.
     * UIKit re-claims the edge recognizer's delegate around transitions, so a
     * one-time assignment ends up ignored: "gesture disabled" entries would
     * swipe back, and hidden-bar entries would lose the gesture (UIKit's
     * default delegate refuses it when the bar is hidden; ours allows it).
     *
     * The iOS 26 content recognizer is only switched on and off. It keeps
     * UIKit's delegate, which decides where in the content a swipe may start.
     */
    private fun refreshPopGesture() {
        val canPop = canPopTopEntry()
        managedGestures.forEach { kind ->
            when (kind) {
                PopGestureKind.Edge -> uiNavigationController.interactivePopGestureRecognizer?.let { recognizer ->
                    recognizer.delegate = gestureDelegate
                    recognizer.enabled = canPop
                }
                PopGestureKind.Content ->
                    uiNavigationController.contentPopGestureRecognizer()?.enabled = canPop
            }
        }
    }
}

/**
 * `interactiveContentPopGestureRecognizer` (iOS 26): the full-width swipe back. Read through a
 * selector check: Kotlin/Native binds the accessor whatever the system version, and iOS 18 raises
 * "unrecognized selector" for it (seen in the watchdog after a push whose `didShow` never came).
 */
private fun UINavigationController.contentPopGestureRecognizer(): UIGestureRecognizer? =
    if (isIOSVersionAtLeast(CONTENT_POP_GESTURE_MIN_IOS) &&
        respondsToSelector(sel_registerName("interactiveContentPopGestureRecognizer"))
    ) {
        interactiveContentPopGestureRecognizer
    } else {
        null
    }

private const val CONTENT_POP_GESTURE_MIN_IOS = 26

private class PopGestureDelegate(
    private val canPop: () -> Boolean,
) : NSObject(), UIGestureRecognizerDelegateProtocol {
    override fun gestureRecognizerShouldBegin(gestureRecognizer: UIGestureRecognizer): Boolean = canPop()
}

private class NavigationDelegate(
    private val onWillShow: (animated: Boolean) -> Unit,
    private val onDidShow: () -> Unit,
) : NSObject(), UINavigationControllerDelegateProtocol {
    // Both selectors map to the same Kotlin signature; the annotation keeps
    // them distinct for the Objective-C bridge.
    @ObjCSignatureOverride
    override fun navigationController(
        navigationController: UINavigationController,
        willShowViewController: UIViewController,
        animated: Boolean,
    ) {
        onWillShow(animated)
    }

    @ObjCSignatureOverride
    override fun navigationController(
        navigationController: UINavigationController,
        didShowViewController: UIViewController,
        animated: Boolean,
    ) {
        onDidShow()
    }
}
