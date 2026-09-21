package com.mohamedrejeb.calf.navigation.internal

/** What an entry declared about its place in a list-detail layout, without the role's payload. */
internal enum class PaneRoleKind { None, List, Detail }

/**
 * A back stack cut into its list side and its detail side.
 *
 * @property primary everything up to the detail side, the list included.
 * @property secondary the first detail entry and everything pushed after it.
 * @property list the list entry whose placeholder stands in for an empty detail side: the topmost
 *   list of [primary]. `null` when the stack holds no list entry, which also means it is not cut.
 */
internal data class ListDetailPartition<T>(
    val primary: List<T>,
    val secondary: List<T>,
    val list: T?,
)

/**
 * Cuts [stack] at the first detail entry that comes after the first list entry.
 *
 * - No list entry: nothing is cut, however the other entries are marked. A detail needs a list to
 *   sit next to, so a deep link has to put the list below the detail.
 * - Entries without a role follow their position: below the cut they stay on the list side (a
 *   filter screen pushed over the list), above it on the detail side (a detail's own sub-stack).
 * - Roles above the cut are not looked at, so a list opened from a detail stays on the detail side.
 *
 * Items are cut by position, never compared, so the same key can be on the stack twice.
 *
 * Pure function, no platform types: it mirrors [reconcileNavStack] and is verified in commonTest.
 */
internal fun <T> partitionListDetail(
    stack: List<T>,
    roleOf: (T) -> PaneRoleKind,
): ListDetailPartition<T> {
    val firstList = stack.indexOfFirst { roleOf(it) == PaneRoleKind.List }
    if (firstList < 0) return ListDetailPartition(primary = stack, secondary = emptyList(), list = null)

    val firstDetail = (firstList + 1..stack.lastIndex).firstOrNull { roleOf(stack[it]) == PaneRoleKind.Detail }
    val cut = firstDetail ?: stack.size
    val primary = stack.take(cut)

    return ListDetailPartition(
        primary = primary,
        secondary = stack.drop(cut),
        list = primary.last { roleOf(it) == PaneRoleKind.List },
    )
}

/** The pane a key is shown in while two panes are visible. */
internal enum class PaneSide { List, Detail }

/**
 * The pane of every key of [stack], for a host that is told about entries one key at a time (the
 * Material scene reads a role from each entry's metadata).
 *
 * Entries below the first list entry have no side, and a stack without a list entry has none at
 * all: such entries are shown full screen. A key that is on both sides counts as detail, the side
 * of its topmost occurrence.
 */
internal fun <T> paneSidesOf(
    stack: List<T>,
    roleOf: (T) -> PaneRoleKind,
): Map<T, PaneSide> {
    val partition = partitionListDetail(stack, roleOf)
    if (partition.list == null) return emptyMap()

    val firstList = partition.primary.indexOfFirst { roleOf(it) == PaneRoleKind.List }
    val listSide = partition.primary.drop(firstList).associateWith { PaneSide.List }
    val detailSide = partition.secondary.associateWith { PaneSide.Detail }
    return listSide + detailSide
}

/**
 * Which container shows which entries for a pane count.
 *
 * With one pane everything is in [compact], in back stack order, so its top is the detail side's
 * top when a detail is open and the list otherwise. With two panes [primary] and [secondary] hold
 * the two sides and [compact] is empty. Together they always hold every entry exactly once: a
 * pane count change re-arranges entries and never adds or drops one.
 *
 * @property placeholderList the list entry whose placeholder fills the empty detail pane, `null`
 *   when no placeholder is shown.
 */
internal data class ListDetailArrangement<T>(
    val primary: List<T>,
    val secondary: List<T>,
    val compact: List<T>,
    val placeholderList: T?,
)

/** Arranges [stack] for [paneCount] panes. Anything above two is two: there is no third pane. */
internal fun <T> arrangeListDetail(
    stack: List<T>,
    roleOf: (T) -> PaneRoleKind,
    paneCount: Int,
): ListDetailArrangement<T> {
    if (paneCount < TWO_PANES) {
        return ListDetailArrangement(
            primary = emptyList(),
            secondary = emptyList(),
            compact = stack,
            placeholderList = null,
        )
    }

    val partition = partitionListDetail(stack, roleOf)
    return ListDetailArrangement(
        primary = partition.primary,
        secondary = partition.secondary,
        compact = emptyList(),
        placeholderList = partition.list.takeIf { partition.secondary.isEmpty() },
    )
}

/**
 * [stack] after the user picked [detail] in [list]: whatever is above the topmost [list] is
 * replaced by [detail]. A stack without [list] just gets [detail] pushed.
 */
internal fun <T> stackWithDetail(stack: List<T>, list: T, detail: T): List<T> {
    val listIndex = stack.lastIndexOf(list)
    val kept = if (listIndex < 0) stack else stack.take(listIndex + 1)
    return kept + detail
}

private const val TWO_PANES = 2
