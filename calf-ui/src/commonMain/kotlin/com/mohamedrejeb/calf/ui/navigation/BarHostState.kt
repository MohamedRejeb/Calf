package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Immutable

/*
 * Who owns the native bars of an iOS bar host, and what they show. Pure rules, no UIKit, so they
 * can be unit tested; the iOS host only feeds scaffolds, gestures and measurements in.
 *
 * The host is one UIKit container for the whole Compose hierarchy. Every AdaptiveScaffold in it
 * registers; the newest one owns the bars and the container shows its top bar, toolbar and tab
 * bar. Screens change inside Compose, so "newest" is the screen that composed last: the entering
 * one during a push or a pop. A pane that composes beside the owner (a list next to a detail)
 * does not take its bars.
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
    private val generation: Long,
    private val gesture: GestureFreeze?,
    private val barShares: Map<Any, IosBarEdgeInsets>,
) {
    constructor() : this(emptyMap(), 0L, 0L, null, emptyMap())

    /**
     * The scaffold whose bars are shown: the newest candidate, except while a back gesture runs.
     *
     * Scaffolds that appear together (two panes, a restored back stack) are equally new; among
     * them one with bars wins. A scaffold without bars only becomes a candidate once it is placed,
     * and does not take the bars from a scaffold with bars it sits beside: it is a pane next to the
     * owner, not a screen over it.
     *
     * Navigation 3 composes the previous screen as soon as a back gesture starts, and it only
     * becomes the screen once the gesture commits; a cancelled gesture must never show its bars.
     */
    val owner: Any? =
        gesture?.owner?.takeIf { it in scaffolds } ?: newest()

    /** The owner, then the scaffolds it is nested in, outwards: the scaffolds whose bars are shown. */
    private val shownChain: List<RegisteredScaffold> = ownerChain()

    /** The owner's bars; slots it leaves empty come from the scaffolds it is nested in. */
    val shownBars: HostedBars =
        shownChain.fold(HostedBars()) { shown, scaffold ->
            HostedBars(
                topBar = shown.topBar ?: scaffold.bars.topBar,
                toolbarItems = shown.toolbarItems ?: scaffold.bars.toolbarItems,
                tabBar = shown.tabBar ?: scaffold.bars.tabBar,
            )
        }

    /** The scaffold the shown tab bar belongs to, which receives its selection. */
    val shownTabBarSource: Any? =
        shownChain.firstOrNull { it.bars.tabBar != null }?.key

    /** Whether a registered scaffold publishes a tab bar, shown or not (a screen pushed over tabs). */
    val hasTabs: Boolean =
        scaffolds.values.any { it.bars.tabBar != null }

    /** Where the owner sits across the host, `null` until it is placed. */
    val ownerSpan: HostedSpan? =
        owner?.let(scaffolds::get)?.span

    /** Where [key] sits across the host, `null` until it is placed. */
    fun spanOf(key: Any): HostedSpan? = scaffolds[key]?.span

    /** Adds [key], nested in [parent]. A scaffold keeps the position of its first registration. */
    fun register(key: Any, parent: Any?): BarHostState {
        if (key in scaffolds) return this
        val registered = RegisteredScaffold(
            key = key,
            parent = parent,
            order = nextOrder,
            generation = generation,
            bars = HostedBars(),
        )
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
            barShares = barShares - key,
        )
    }

    fun withBars(key: Any, transform: (HostedBars) -> HostedBars): BarHostState {
        val scaffold = scaffolds[key] ?: return this
        val bars = transform(scaffold.bars)
        if (bars == scaffold.bars) return this
        return copy(scaffolds = scaffolds + (key to scaffold.copy(bars = bars)))
    }

    /** Records where [key] was placed across the host. */
    fun withSpan(key: Any, span: HostedSpan?): BarHostState {
        val scaffold = scaffolds[key] ?: return this
        val clamped = span?.clamped()
        if (clamped == scaffold.span) return this
        return copy(scaffolds = scaffolds + (key to scaffold.copy(span = clamped)))
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

    /**
     * Ends the current generation: scaffolds registered from now on are newer than every one
     * registered so far. The host calls it once per main-thread turn, after applying the bars.
     */
    fun nextGeneration(): BarHostState = copy(generation = generation + 1)

    /**
     * Records what the shown bars add to the [system] insets ([bars] is the content area the
     * containers leave with them), for every scaffold whose bars are shown. A scaffold keeps its
     * share once its bars are no longer shown, until it leaves.
     */
    fun withMeasuredInsets(bars: IosBarEdgeInsets, system: IosBarEdgeInsets): BarHostState {
        val share = bars.beyond(system)
        val updated = shownChain.filter { barShares[it.key] != share }
        if (updated.isEmpty()) return this
        return copy(barShares = barShares + updated.map { it.key to share })
    }

    /**
     * Content insets for [key]. A scaffold whose bars are shown follows the containers ([bars]).
     * Any other one gets the [system] insets plus the share of the bars it had when they were its
     * own: a leaving screen does not jump when the bars change under it (a large title appearing,
     * the toolbar leaving), for as long as it is composed, and a pane beside the owner never gets
     * the owner's bar.
     */
    fun contentInsetsOf(key: Any, bars: IosBarEdgeInsets, system: IosBarEdgeInsets): IosBarEdgeInsets =
        if (shownChain.any { it.key == key }) bars else system + (barShares[key] ?: IosBarEdgeInsets.Zero)

    private fun newest(): Any? {
        val candidates = scaffolds.values
            .filter { it.bars.hasAny || it.span != null }
            .sortedWith(compareByDescending<RegisteredScaffold> { it.generation }.thenByDescending { it.bars.hasAny }.thenByDescending { it.order })
        return candidates.firstOrNull { candidate ->
            candidate.bars.hasAny || candidates.none { it.bars.hasAny && it.isBeside(candidate) }
        }?.key
    }

    /** The owner, then the scaffolds it is nested in, outwards; ancestors that left are skipped. */
    private fun ownerChain(): List<RegisteredScaffold> =
        generateSequence(owner?.let(scaffolds::get)) { scaffold ->
            generateSequence(scaffold.parent) { scaffolds[it]?.parent }
                .firstNotNullOfOrNull { scaffolds[it] }
        }.toList()

    private fun copy(
        scaffolds: Map<Any, RegisteredScaffold> = this.scaffolds,
        nextOrder: Long = this.nextOrder,
        generation: Long = this.generation,
        gesture: GestureFreeze? = this.gesture,
        barShares: Map<Any, IosBarEdgeInsets> = this.barShares,
    ): BarHostState = BarHostState(scaffolds, nextOrder, generation, gesture, barShares)

    override fun equals(other: Any?): Boolean =
        other is BarHostState &&
            other.scaffolds == scaffolds &&
            other.nextOrder == nextOrder &&
            other.generation == generation &&
            other.gesture == gesture &&
            other.barShares == barShares

    override fun hashCode(): Int =
        listOf(scaffolds, nextOrder, generation, gesture, barShares).hashCode()

    override fun toString(): String =
        "BarHostState(scaffolds=${scaffolds.size}, owner=$owner, frozen=${gesture != null})"
}

/**
 * Whether the large-title calibration has to start over: the shown top bar appeared, hid or
 * switched title style, or a new screen or container shows it ([isNewLayout]). A calibration kept
 * after its bar hid would pad a bar-less screen by the height the bar had.
 */
internal fun topBarNeedsRecalibration(previous: HostedTopBar?, next: HostedTopBar?, isNewLayout: Boolean): Boolean =
    when {
        next == null -> previous != null
        isNewLayout || previous == null -> true
        else -> previous.configuration.prefersLargeTitles != next.configuration.prefersLargeTitles
    }

private data class RegisteredScaffold(
    val key: Any,
    val parent: Any?,
    val order: Long,
    val generation: Long,
    val bars: HostedBars,
    val span: HostedSpan? = null,
) {
    /** Placed side by side with [other]; unknown while either one is not placed yet. */
    fun isBeside(other: RegisteredScaffold): Boolean {
        val mine = span ?: return false
        val theirs = other.span ?: return false
        return mine.isBeside(theirs)
    }
}

private val HostedBars.hasAny: Boolean
    get() = topBar != null || toolbarItems != null || tabBar != null

/** The owner held while a back gesture runs, and the registrations it started with. */
private data class GestureFreeze(
    val owner: Any,
    val fromOrder: Long,
    val hasEnded: Boolean = false,
) {
    fun isSettled(scaffolds: Map<Any, RegisteredScaffold>): Boolean =
        owner !in scaffolds || (hasEnded && scaffolds.values.none { it.order >= fromOrder })
}
