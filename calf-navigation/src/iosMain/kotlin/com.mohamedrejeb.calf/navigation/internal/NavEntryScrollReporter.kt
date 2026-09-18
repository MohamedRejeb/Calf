package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Receives the entry content's vertical scroll offset in pixels. Provided by
 * `EntryHostingController`, which mirrors it into the controller's content
 * scroll view so UIKit collapses the large title and updates the bar's
 * scroll-edge appearance. `null` outside a hosted entry.
 */
internal val LocalNavEntryScrollReporter = staticCompositionLocalOf<((Float) -> Unit)?> { null }
