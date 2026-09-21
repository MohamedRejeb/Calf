package com.mohamedrejeb.calf.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.internal.stackWithDetail

/**
 * Opens [detail] from [list] in a list-detail layout: whatever is on the stack above [list] (the
 * previous detail and the screens it pushed) is replaced by [detail].
 *
 * With one pane this is an ordinary push from the list. With two panes it is what keeps the back
 * stack from growing by one detail per tap, since the list stays visible next to the open detail:
 *
 * ```kotlin
 * onItemClick = { item -> backStack.selectDetail(list = Inbox, detail = Message(item.id)) }
 * ```
 *
 * Selecting the detail that is already on top changes nothing. If [list] is not on the stack,
 * [detail] is pushed.
 */
@ExperimentalCalfNavigationApi
fun NavBackStack<NavKey>.selectDetail(list: NavKey, detail: NavKey) {
    val current = toList()
    val target = stackWithDetail(current, list = list, detail = detail)
    if (target == current) return

    // Only the tail differs: the entries below keep their place, and with it their state.
    val unchanged = current.zip(target).takeWhile { (before, after) -> before == after }.size
    subList(unchanged, size).clear()
    addAll(target.drop(unchanged))
}
