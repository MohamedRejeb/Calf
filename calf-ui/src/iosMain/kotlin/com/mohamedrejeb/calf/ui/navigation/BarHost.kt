package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import platform.UIKit.UIViewController

/**
 * The bar host of one [AdaptiveBarHostUIViewController]: the scaffolds registered in its Compose
 * surface ([BarHostState]), the insets the container measured, and the callbacks UIKit hands
 * back (tab selection, scrolling). Main thread only.
 *
 * @param contentViewController the one Compose view controller of the container. Only scaffolds
 *   composed in it take part; a sheet's or a calf-navigation entry's content has its own.
 * @param onBarsChange asks the container to apply the bars again, once per main-thread turn.
 */
internal class BarHost(
    val contentViewController: UIViewController,
    private val onBarsChange: () -> Unit,
    private val onOwnerScrollChange: (Float) -> Unit,
) {
    var state: BarHostState by mutableStateOf(BarHostState())
        private set

    /**
     * The container's content insets, four edges, the top one calibrated for the large title.
     * `null` until the container measured itself.
     */
    private var liveInsets: IosBarEdgeInsets? by mutableStateOf(null)

    /** Points per pixel of the Compose surface, for the scroll offsets handed to UIKit. */
    var density: Float = 1f

    private val tabSelection = mutableMapOf<Any, (Int) -> Unit>()
    private val scrollOffsets = mutableMapOf<Any, Float>()

    fun isHostSurface(viewController: UIViewController): Boolean =
        viewController === contentViewController

    fun register(key: Any, parent: Any?) = update { it.register(key, parent) }

    fun unregister(key: Any) {
        tabSelection.remove(key)
        scrollOffsets.remove(key)
        update { it.unregister(key) }
    }

    fun updateBars(key: Any, transform: (HostedBars) -> HostedBars) = update { it.withBars(key, transform) }

    fun setBackGestureInProgress(inProgress: Boolean) = update { it.withBackGesture(inProgress) }

    fun setLiveInsets(insets: IosBarEdgeInsets) {
        if (insets == liveInsets) return
        liveInsets = insets
        update { it }
    }

    /** Content insets of [key] in the host's coordinates, `null` before the first measurement. */
    fun contentInsetsOf(key: Any): IosBarEdgeInsets? =
        liveInsets?.let { state.contentInsetsOf(key, it) }

    fun setTabSelection(key: Any, onItemSelected: (Int) -> Unit) {
        tabSelection[key] = onItemSelected
    }

    /** A tab was selected in the container: tell the scaffold whose tab bar is shown. */
    fun onTabSelected(index: Int) {
        state.shownTabBarSource?.let { tabSelection[it] }?.invoke(index)
    }

    /** [offsetPx] is how far [key]'s content scrolled from its top. Only the owner's reaches UIKit. */
    fun reportScroll(key: Any, offsetPx: Float) {
        scrollOffsets[key] = offsetPx
        if (key == state.owner) onOwnerScrollChange(offsetPx / density)
    }

    private fun update(transform: (BarHostState) -> BarHostState) {
        val old = state
        // The owner always records its insets, so it keeps them once another scaffold takes over.
        val new = transform(old).let { next -> liveInsets?.let(next::withLiveInsets) ?: next }
        if (new == old) return
        state = new
        if (new.owner != old.owner) {
            onOwnerScrollChange((new.owner?.let(scrollOffsets::get) ?: 0f) / density)
        }
        onBarsChange()
    }
}

/** The bar host of the Compose surface, `null` when the app did not install one. */
internal val LocalAdaptiveBarHost = staticCompositionLocalOf<BarHost?> { null }

/** The hosted [AdaptiveScaffold] around the current composable, `null` outside one. */
internal val LocalHostedScaffold = staticCompositionLocalOf<HostedScaffold?> { null }

/**
 * One [AdaptiveScaffold] registered with a [BarHost]. It is also the key of its entry: it
 * registers when remembered and leaves when forgotten, including a composition that is abandoned.
 */
internal class HostedScaffold(
    val host: BarHost,
    private val parent: HostedScaffold?,
) : RememberObserver {

    val scrollTracker = LargeTitleScrollTracker { offsetPx -> host.reportScroll(this, offsetPx) }

    fun updateBars(transform: (HostedBars) -> HostedBars) = host.updateBars(this, transform)

    override fun onRemembered() = host.register(this, parent)

    override fun onForgotten() = host.unregister(this)

    override fun onAbandoned() = host.unregister(this)
}

/**
 * The hosted scaffold a bar in the current composition publishes to, or `null` when the bar has
 * to draw itself: no host installed, no hosted scaffold around it, or composed in another Compose
 * surface (a sheet's content gets the parent's locals re-applied, so the locals alone are not enough).
 */
@Composable
internal fun currentHostedScaffold(): HostedScaffold? {
    val scaffold = LocalHostedScaffold.current ?: return null
    return scaffold.takeIf { it.host.isHostSurface(LocalUIViewController.current) }
}

/**
 * Keeps [publish]'s slot of [scaffold]'s bars up to date for as long as the caller is composed,
 * and empties it with [withdraw] when it leaves.
 */
@Composable
internal fun PublishHostedBar(
    scaffold: HostedScaffold,
    publish: (HostedBars) -> HostedBars,
    withdraw: (HostedBars) -> HostedBars,
) {
    SideEffect { scaffold.updateBars(publish) }
    DisposableEffect(scaffold) {
        onDispose { scaffold.updateBars(withdraw) }
    }
}

/** Freezes the bar owner while a back gesture runs (see [BarHostState.withBackGesture]). */
@Composable
internal fun ObserveBackGesture(host: BarHost) {
    val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher ?: return
    LaunchedEffect(dispatcher, host) {
        dispatcher.transitionState.collect { transition ->
            host.setBackGestureInProgress(transition is NavigationEventTransitionState.InProgress)
        }
    }
}
