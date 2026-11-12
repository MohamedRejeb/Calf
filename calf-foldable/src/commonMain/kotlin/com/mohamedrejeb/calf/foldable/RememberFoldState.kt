package com.mohamedrejeb.calf.foldable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Replaces what [rememberFoldState] returns below it. Meant for previews and tests:
 * ```
 * CompositionLocalProvider(LocalFoldStateOverride provides FoldState(halfOpenSnapshot)) {
 *     MyFoldAwareScreen()
 * }
 * ```
 */
@ExperimentalCalfFoldableApi
val LocalFoldStateOverride: ProvidableCompositionLocal<FoldState?> = staticCompositionLocalOf { null }

/**
 * Observes the fold of the display this composition is shown on, for as long as the caller stays
 * in the composition.
 *
 * - Android: Jetpack WindowManager's `FoldingFeature` of the hosting activity.
 * - iOS: `UIHingeInteraction` and the reserved regions of the hosting view (iOS 27.1 and later;
 *   regions also need the app to be built with the iOS 27.1 SDK). Older versions report no fold.
 * - Desktop and web: always [FoldSnapshot.NoFold].
 */
@ExperimentalCalfFoldableApi
@Composable
fun rememberFoldState(): FoldState =
    LocalFoldStateOverride.current ?: rememberPlatformFoldState()

@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
internal expect fun rememberPlatformFoldState(): FoldState
