package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf

/**
 * Safe-area insets of the current entry's hosting `UIViewController`,
 * measured from UIKit (`view.safeAreaInsets`) and converted to dp.
 *
 * This is the ground truth for how much of the entry is covered by the
 * navigation bar (plus status bar and home indicator): it includes the
 * nav-bar extension `UINavigationController` applies to its children — which
 * the child Compose runtime's own `WindowInsets` does not reliably reflect —
 * and it updates when the bar hides ([AdaptiveNavEntryMetadata.barShown]) or changes
 * height.
 *
 * `null` when the entry is not hosted by an `EntryHostingController`
 * (e.g. in tests); consumers fall back to `WindowInsets.safeDrawing`.
 */
internal val LocalNavEntrySafeArea = compositionLocalOf<PaddingValues?> { null }
