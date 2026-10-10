package com.mohamedrejeb.calf.navigation.internal

import androidx.navigation3.runtime.NavKey

/** Outcome of [decideNavChange]. */
internal sealed interface NavChangeDecision {
    /** Apply the change now. */
    data object Accept : NavChangeDecision

    /** Apply the change once the running transition has ended. */
    data object Defer : NavChangeDecision

    /** Drop the change by restoring [stack], what is on screen. */
    data class Revert(val stack: List<NavKey>) : NavChangeDecision
}

/**
 * Decides what to do with a back-stack change while a native navigation transition may be
 * running. `UINavigationController` cannot start a transition inside another one, so a change
 * that arrives mid-transition is not applied at once.
 *
 * - Not transitioning, or nothing changed: [NavChangeDecision.Accept].
 * - Transitioning, and the change only pushes the entry that is already being shown again (a
 *   double tap on the row that opened it): [NavChangeDecision.Revert], the second push is dropped.
 * - Transitioning, any other change (a pop, a reset after a login, a deep link, a push of
 *   something else): [NavChangeDecision.Defer], the host re-reads the back stack when the
 *   transition ends. Nothing the caller did is lost, it is only shown later.
 *
 * Only the iOS host uses it. Material keeps Navigation 3's own behaviour.
 */
internal fun decideNavChange(
    accepted: List<NavKey>,
    snapshot: List<NavKey>,
    isTransitioning: Boolean,
): NavChangeDecision = when {
    !isTransitioning || snapshot == accepted -> NavChangeDecision.Accept
    isRepeatedTopPush(accepted, snapshot) -> NavChangeDecision.Revert(accepted)
    else -> NavChangeDecision.Defer
}

/** [snapshot] is [accepted] with its own top entry pushed again, once or more. */
private fun isRepeatedTopPush(accepted: List<NavKey>, snapshot: List<NavKey>): Boolean {
    val top = accepted.lastOrNull() ?: return false
    if (snapshot.size <= accepted.size || snapshot.take(accepted.size) != accepted) return false
    return snapshot.drop(accepted.size).all { it == top }
}
