package com.mohamedrejeb.calf.navigation.internal

/**
 * The swipe-back recognizers a `UINavigationController` owns. Every one of
 * them has to follow the top entry's `iosGestureEnabled`: a recognizer left
 * alone keeps UIKit's defaults and pops entries that opted out.
 *
 * @property minIosVersion first iOS major version that has the recognizer.
 *   Its selector does not exist below that, so it must not be touched there.
 */
internal enum class PopGestureKind(val minIosVersion: Int) {
    /** `interactivePopGestureRecognizer`: swipe from the leading screen edge. Always present. */
    Edge(minIosVersion = 0),

    /** `interactiveContentPopGestureRecognizer`: swipe from anywhere in the content. */
    Content(minIosVersion = 26),
}

/**
 * The recognizers that exist on the running system and therefore have to be
 * managed. [isIOSVersionAtLeast] answers whether the system is at least the
 * given major version.
 */
internal fun managedPopGestures(isIOSVersionAtLeast: (major: Int) -> Boolean): List<PopGestureKind> =
    PopGestureKind.entries.filter { kind -> isIOSVersionAtLeast(kind.minIosVersion) }

/**
 * Whether swipe-back may start: there is something to pop to, and the top
 * entry did not opt out. [topGestureEnabled] is `null` when the top controller
 * is not a navigator entry, which keeps UIKit's default. Bar visibility is
 * deliberately not an input: hidden-bar entries still swipe back.
 */
internal fun canPopInteractively(stackSize: Int, topGestureEnabled: Boolean?): Boolean =
    stackSize > 1 && topGestureEnabled != false
