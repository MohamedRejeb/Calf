package com.mohamedrejeb.calf.navigation

/**
 * Marks navigation APIs in Calf as experimental. The shape of these APIs may
 * change in a non-binary-compatible way before they are graduated to stable.
 *
 * Use them by opting in:
 * ```
 * @OptIn(ExperimentalCalfNavigationApi::class)
 * fun MyScreen() { ... }
 * ```
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This Calf navigation API is experimental and may change in a future release.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
annotation class ExperimentalCalfNavigationApi
