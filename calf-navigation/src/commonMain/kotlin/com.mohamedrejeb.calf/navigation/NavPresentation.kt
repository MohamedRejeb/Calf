package com.mohamedrejeb.calf.navigation

/**
 * Per-entry presentation style.
 *
 * - [Card] is the default — the entry is pushed onto the navigation stack with
 *   the platform's standard transition (slide on iOS, predictive-back on Android).
 * - [FormSheet], [PageSheet] and [FullScreenModal] are iOS-style modal presentation
 *   styles. **They are reserved for a future release; in the walking-skeleton
 *   prototype every value falls back to [Card].**
 *
 * @see adaptiveNavEntry
 */
@ExperimentalCalfNavigationApi
enum class NavPresentation {
    /** Push onto the navigation stack (default behaviour). */
    Card,

    /** iOS form-sheet modal. Falls back to [Card] in v1. */
    FormSheet,

    /** iOS page-sheet modal. Falls back to [Card] in v1. */
    PageSheet,

    /** iOS full-screen modal. Falls back to [Card] in v1. */
    FullScreenModal,
}
