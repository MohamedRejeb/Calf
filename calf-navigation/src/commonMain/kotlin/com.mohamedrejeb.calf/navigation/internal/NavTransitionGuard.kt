package com.mohamedrejeb.calf.navigation.internal

import androidx.navigation3.runtime.NavKey

/** Outcome of [decideNavChange]. */
internal sealed interface NavChangeDecision {
    /** Apply the change. */
    data object Accept : NavChangeDecision

    /** Drop the change by restoring [stack], what is on screen. */
    data class Revert(val stack: List<NavKey>) : NavChangeDecision
}

/**
 * Decides whether a back-stack change may be applied while a navigation
 * transition may be running. Like Flutter, navigation requested during an
 * animation is ignored, which is what turns a double tap into a single push.
 *
 * - Not transitioning, or nothing changed: accept.
 * - Transitioning: a pure pop ([snapshot] is a strict prefix of [accepted])
 *   is accepted when [allowPops] is set, because Material's predictive back
 *   pops while its own transition is still running; everything else reverts
 *   to [accepted].
 */
internal fun decideNavChange(
    accepted: List<NavKey>,
    snapshot: List<NavKey>,
    isTransitioning: Boolean,
    allowPops: Boolean,
): NavChangeDecision {
    if (!isTransitioning || snapshot == accepted) return NavChangeDecision.Accept
    val isPop = snapshot.size < accepted.size && accepted.subList(0, snapshot.size) == snapshot
    return if (allowPops && isPop) NavChangeDecision.Accept else NavChangeDecision.Revert(accepted)
}
