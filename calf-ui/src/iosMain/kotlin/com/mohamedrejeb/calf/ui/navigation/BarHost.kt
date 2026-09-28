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
 * @param onBarsChange asks the container to apply the bars again. `immediate` when the owner or
 *   the shown bars changed: the container applies and measures right away, so a screen composing
 *   its content in the same layout pass already gets the insets of its own bars. Anything else,
 *   the owner's column included (it moves on every frame of a pane's slide), is applied once per
 *   main-thread turn.
 */
internal class BarHost(
    val contentViewController: UIViewController,
    private val onBarsChange: (immediate: Boolean) -> Unit,
    private val onOwnerScrollChange: (Float) -> Unit,
) {
    var state: BarHostState by mutableStateOf(BarHostState())
        private set

    /**
     * What the containers leave to the content with the shown bars, four edges, the top one
     * calibrated for the large title. `null` until the container measured itself.
     */
    private var barInsets: IosBarEdgeInsets? by mutableStateOf(null)

    /** The window's safe area without the containers' bars: what the other scaffolds follow. */
    private var systemInsets: IosBarEdgeInsets? by mutableStateOf(null)

    /** Points per pixel of the Compose surface, for the scroll offsets handed to UIKit. */
    var density: Float = 1f

    private val tabSelection = mutableMapOf<Any, (Int) -> Unit>()
    private var hasRegistrationsSinceApply = false
    private val scrollOffsets = mutableMapOf<Any, Float>()

    /** Whether the container measured itself; until then scaffolds keep Compose's own insets. */
    var isMeasured: Boolean by mutableStateOf(false)
        private set

    fun isHostSurface(viewController: UIViewController): Boolean =
        viewController === contentViewController

    fun register(key: Any, parent: Any?) {
        hasRegistrationsSinceApply = true
        update { it.register(key, parent) }
    }

    fun unregister(key: Any) {
        hasRegistrationsSinceApply = true
        tabSelection.remove(key)
        scrollOffsets.remove(key)
        update { it.unregister(key) }
    }

    fun updateBars(key: Any, transform: (HostedBars) -> HostedBars) = update { it.withBars(key, transform) }

    fun setBackGestureInProgress(inProgress: Boolean) = update { it.withBackGesture(inProgress) }

    /**
     * After the container applied the bars: what registers from now on is newer. Only when
     * scaffolds came or went since the last apply, so a bar that merely recomposed between two
     * panes' registrations cannot split them into two generations.
     */
    fun endGeneration() {
        if (!hasRegistrationsSinceApply) return
        hasRegistrationsSinceApply = false
        state = state.nextGeneration()
    }

    fun setMeasuredInsets(bars: IosBarEdgeInsets, system: IosBarEdgeInsets) {
        if (bars == barInsets && system == systemInsets) return
        barInsets = bars
        systemInsets = system
        isMeasured = true
        update { it }
    }

    /** Where [key] was placed across the host's surface; decides panes beside the owner. */
    fun setSpan(key: Any, span: HostedSpan) = update { it.withSpan(key, span) }

    /**
     * Content insets of [key] in the host's coordinates (see [BarHostState.contentInsetsOf]),
     * `null` before the first measurement.
     */
    fun contentInsetsOf(key: Any): IosBarEdgeInsets? =
        barInsets?.let { bars -> state.contentInsetsOf(key, bars, systemInsets ?: bars) }

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
        // The shown bars' scaffolds record their share, so they keep it once another one takes over.
        val new = transform(old).let { next ->
            barInsets?.let { bars -> next.withMeasuredInsets(bars, systemInsets ?: bars) } ?: next
        }
        if (new == old) return
        state = new
        val shownChanged = new.owner != old.owner || new.shownBars != old.shownBars || new.hasTabs != old.hasTabs
        if (new.owner != old.owner) {
            onOwnerScrollChange((new.owner?.let(scrollOffsets::get) ?: 0f) / density)
        }
        onBarsChange(shownChanged)
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
