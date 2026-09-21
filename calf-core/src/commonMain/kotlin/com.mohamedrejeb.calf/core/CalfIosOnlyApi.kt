package com.mohamedrejeb.calf.core

/**
 * Marks a Calf API that only does something on iOS.
 *
 * Calf keeps such members in common code so they can be called without an `expect`/`actual` of
 * your own, but on every other platform they return the empty value named in their documentation
 * (`null`, an empty list, a flow that never emits). Their names start with `ios`.
 *
 * Opting in is how you confirm that you know this:
 * ```
 * @OptIn(CalfIosOnlyApi::class)
 * val cameraCutouts = foldState.iosOcclusionRegions
 * ```
 *
 * This says nothing about stability. Experimental APIs carry their module's `Experimental*Api`
 * annotation as well.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This Calf API only has an effect on iOS. On other platforms it returns an empty value.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
annotation class CalfIosOnlyApi
