package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Immutable

/*
 * Who owns the native bars of an iOS bar host, and what they show. Pure rules, no UIKit, so they
 * can be unit tested; the iOS host only feeds scaffolds, gestures and measurements in.
 *
 * The host is one UIKit container for the whole Compose hierarchy. Every AdaptiveScaffold in it
 * registers; the newest one owns the bars and the container shows its top bar, toolbar and tab
 * bar. Screens change inside Compose, so "newest" is the screen that composed last: the entering
 * one during a push or a pop.
 */

/** What an [AdaptiveTopBar] asks the host's navigation bar to show. */
@Immutable
internal data class HostedTopBar(
    val title: String = "",
    val leadingItems: List<UIKitUIBarButtonItem> = emptyList(),
    val trailingItems: List<UIKitUIBarButtonItem> = emptyList(),
    val configuration: UIKitNavigationBarConfiguration = UIKitNavigationBarConfiguration(),
)

/** What an [AdaptiveNavigationBar] asks the host's tab bar to show. Selection callbacks live in the host. */
@Immutable
internal data class HostedTabBar(
    val items: List<UIKitUITabBarItem>,
    val selectedIndex: Int,
    val configuration: UIKitTabBarConfiguration = UIKitTabBarConfiguration(),
)

/** The bars of one scaffold, one per slot; `null` means the scaffold leaves the slot empty. */
@Immutable
internal data class HostedBars(
    val topBar: HostedTopBar? = null,
    val toolbarItems: List<UIKitUIBarButtonItem>? = null,
    val tabBar: HostedTabBar? = null,
)

/**
 * The scaffolds registered with one host, and which of them owns the bars.
 *
 * Immutable: every change returns a new state, so the host can compare the bars it last applied
 * with the ones it should show now.
 */
@Immutable
internal class BarHostState private constructor(
    private val scaffolds: Map<Any, RegisteredScaffold>,
    private val nextOrder: Long,
    private val gesture: GestureFreeze?,
    private val ownedInsets: Map<Any, IosBarEdgeInsets>,
) {
    constructor() : this(emptyMap(), 0L, null, emptyMap())

    /**
     * The scaffold whose bars are shown: the newest registered one, except while a back gesture
     * runs. Navigation 3 composes the previous screen as soon as the gesture starts, and it only
     * becomes the screen once the gesture commits; a cancelled gesture must never show its bars.
     */
    val owner: Any? =
        gesture?.owner?.takeIf { it in scaffolds } ?: newest()

    /** The owner's bars; slots it leaves empty come from the scaffolds it is nested in. */
    val shownBars: HostedBars =
        ownerChain().fold(HostedBars()) { shown, scaffold ->
            HostedBars(
                topBar = shown.topBar ?: scaffold.bars.topBar,
                toolbarItems = shown.toolbarItems ?: scaffold.bars.toolbarItems,
                tabBar = shown.tabBar ?: scaffold.bars.tabBar,
            )
        }

    /** The scaffold the shown tab bar belongs to, which receives its selection. */
    val shownTabBarSource: Any? =
        ownerChain().firstOrNull { it.bars.tabBar != null }?.key

    /** Adds [key], nested in [parent]. A scaffold keeps the position of its first registration. */
    fun register(key: Any, parent: Any?): BarHostState {
        if (key in scaffolds) return this
        val registered = RegisteredScaffold(key = key, parent = parent, order = nextOrder, bars = HostedBars())
        return copy(
            scaffolds = scaffolds + (key to registered),
            nextOrder = nextOrder + 1,
            // A screen that composes after a finished gesture is new navigation, not the gesture's.
            gesture = gesture?.takeUnless { it.hasEnded },
        )
    }

    fun unregister(key: Any): BarHostState {
        if (key !in scaffolds) return this
        val remaining = scaffolds - key
        return copy(
            scaffolds = remaining,
            gesture = gesture?.takeUnless { it.isSettled(remaining) },
            ownedInsets = ownedInsets - key,
        )
    }

    fun withBars(key: Any, transform: (HostedBars) -> HostedBars): BarHostState {
        val scaffold = scaffolds[key] ?: return this
        val bars = transform(scaffold.bars)
        if (bars == scaffold.bars) return this
        return copy(scaffolds = scaffolds + (key to scaffold.copy(bars = bars)))
    }

    /**
     * A back gesture started or ended. The owner stays frozen after the gesture ends until the
     * outcome settles: the owner leaves (committed) or every screen composed during the gesture
     * leaves again (cancelled).
     */
    fun withBackGesture(inProgress: Boolean): BarHostState =
        when {
            inProgress && gesture == null -> owner?.let { copy(gesture = GestureFreeze(owner = it, fromOrder = nextOrder)) } ?: this
            !inProgress && gesture != null -> copy(gesture = gesture.copy(hasEnded = true).takeUnless { it.isSettled(scaffolds) })
            else -> this
        }

    /** Records [live] as the owner's content insets, the ones it keeps after losing the bars. */
    fun withLiveInsets(live: IosBarEdgeInsets): BarHostState {
        val key = owner ?: return this
        if (ownedInsets[key] == live) return this
        return copy(ownedInsets = ownedInsets + (key to live))
    }

    /**
     * Content insets for [key]: the owner follows the container ([live]); a scaffold that lost the
     * bars keeps the insets it last had, so a leaving screen does not jump when the bar changes
     * height under it (a large title appearing, the toolbar leaving).
     */
    fun contentInsetsOf(key: Any, live: IosBarEdgeInsets): IosBarEdgeInsets =
        if (key == owner) live else ownedInsets[key] ?: live

    private fun newest(): Any? =
        scaffolds.values.maxByOrNull { it.order }?.key

    /** The owner, then the scaffolds it is nested in, outwards; ancestors that left are skipped. */
    private fun ownerChain(): List<RegisteredScaffold> =
        generateSequence(owner?.let(scaffolds::get)) { scaffold ->
            generateSequence(scaffold.parent) { scaffolds[it]?.parent }
                .firstNotNullOfOrNull { scaffolds[it] }
        }.toList()

    private fun copy(
        scaffolds: Map<Any, RegisteredScaffold> = this.scaffolds,
        nextOrder: Long = this.nextOrder,
        gesture: GestureFreeze? = this.gesture,
        ownedInsets: Map<Any, IosBarEdgeInsets> = this.ownedInsets,
    ): BarHostState = BarHostState(scaffolds, nextOrder, gesture, ownedInsets)

    override fun equals(other: Any?): Boolean =
        other is BarHostState &&
            other.scaffolds == scaffolds &&
            other.nextOrder == nextOrder &&
            other.gesture == gesture &&
            other.ownedInsets == ownedInsets

    override fun hashCode(): Int =
        listOf(scaffolds, nextOrder, gesture, ownedInsets).hashCode()

    override fun toString(): String =
        "BarHostState(scaffolds=${scaffolds.size}, owner=$owner, frozen=${gesture != null})"
}

private data class RegisteredScaffold(
    val key: Any,
    val parent: Any?,
    val order: Long,
    val bars: HostedBars,
)

/** The owner held while a back gesture runs, and the registrations it started with. */
private data class GestureFreeze(
    val owner: Any,
    val fromOrder: Long,
    val hasEnded: Boolean = false,
) {
    fun isSettled(scaffolds: Map<Any, RegisteredScaffold>): Boolean =
        owner !in scaffolds || (hasEnded && scaffolds.values.none { it.order >= fromOrder })
}
