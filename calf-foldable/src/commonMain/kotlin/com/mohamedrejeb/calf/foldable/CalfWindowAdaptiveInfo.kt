package com.mohamedrejeb.calf.foldable

import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Material3 Adaptive's [WindowAdaptiveInfo] for the current window, with the fold filled in where
 * Compose Multiplatform leaves it empty.
 *
 * `currentWindowAdaptiveInfo()` reads the fold only on Android. On iOS its `windowPosture` is a
 * hard-coded empty [Posture], so the Material3 adaptive scaffolds never see an iPhone Duo's fold.
 * This returns the same size class with the posture from [rememberFoldState] instead:
 *
 * - Android: upstream's own info, untouched. It already reads the `FoldingFeature`.
 * - iOS: upstream's size class, Calf's posture. An open iPhone Duo reports a vertical hinge.
 * - Desktop and web: upstream's own info. There is no fold to add.
 *
 * Hand it to `calculatePaneScaffoldDirective`, `NavigationSuiteScaffold`, or any other API that
 * takes a [WindowAdaptiveInfo].
 */
@ExperimentalCalfFoldableApi
@Composable
fun rememberCalfWindowAdaptiveInfo(): WindowAdaptiveInfo {
    val upstream = currentWindowAdaptiveInfoV2()
    // A per-platform constant, so the composable call below is made consistently on every recomposition.
    // Where upstream reads the fold itself there is no reason to start a second fold collector.
    if (upstreamReportsWindowPosture) return upstream
    val snapshot = rememberFoldState().snapshot
    return remember(upstream, snapshot) {
        mergeWindowAdaptiveInfo(
            upstream = upstream,
            foldPosture = snapshot.toWindowPosture(),
            upstreamReportsPosture = upstreamReportsWindowPosture,
        )
    }
}

/**
 * [upstream] with its posture replaced by [foldPosture], unless the platform reports its own
 * posture ([upstreamReportsPosture]) or Calf has nothing to add. Both of those return [upstream]
 * itself, so a caller keying on the result sees no change.
 */
internal fun mergeWindowAdaptiveInfo(
    upstream: WindowAdaptiveInfo,
    foldPosture: Posture,
    upstreamReportsPosture: Boolean,
): WindowAdaptiveInfo {
    if (upstreamReportsPosture) return upstream
    if (foldPosture == NoPosture) return upstream
    return WindowAdaptiveInfo(
        windowSizeClass = upstream.windowSizeClass,
        windowPosture = foldPosture,
    )
}

/**
 * Whether Material3 Adaptive's `currentWindowAdaptiveInfo()` reads the fold itself on this
 * platform. Where it does, Calf must not feed the same fold a second time.
 */
internal expect val upstreamReportsWindowPosture: Boolean

private val NoPosture = Posture()
