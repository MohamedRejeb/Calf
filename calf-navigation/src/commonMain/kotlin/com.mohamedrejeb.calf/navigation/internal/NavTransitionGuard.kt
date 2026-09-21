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
 * Decides whether a back-stack change may be applied while a native navigation
 * transition may be running. `UINavigationController` cannot start a transition
 * inside another one, so navigation requested during an animation is ignored,
 * like Flutter does; that is also what turns a double tap into a single push.
 *
 * - Not transitioning, or nothing changed: accept.
 * - Transitioning: revert to [accepted], whatever the change is.
 *
 * Only the iOS host uses it. Material keeps Navigation 3's own behaviour.
 */
internal fun decideNavChange(
    accepted: List<NavKey>,
    snapshot: List<NavKey>,
    isTransitioning: Boolean,
): NavChangeDecision =
    if (!isTransitioning || snapshot == accepted) NavChangeDecision.Accept else NavChangeDecision.Revert(accepted)
