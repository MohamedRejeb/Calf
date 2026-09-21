package com.mohamedrejeb.calf.foldable

import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.runtime.Immutable
import com.mohamedrejeb.calf.core.CalfIosOnlyApi

/**
 * Everything [FoldState] knows at one moment, as an immutable value. Build one by hand to put a
 * preview or a test into any pose:
 * ```
 * FoldState(FoldSnapshot(posture = FoldPosture.HalfOpen, fold = myFold))
 * ```
 *
 * The `ios` members are [CalfIosOnlyApi]: they keep their default on every other platform.
 */
@ExperimentalCalfFoldableApi
@Immutable
data class FoldSnapshot(
    val posture: FoldPosture = FoldPosture.NoFold,
    val fold: FoldInfo? = null,
    @property:CalfIosOnlyApi val iosFoldRegion: FoldRegion? = null,
    @property:CalfIosOnlyApi val iosOcclusionRegions: List<FoldRegion> = emptyList(),
    @property:CalfIosOnlyApi val iosHorizontalSizeClass: IosSizeClass = IosSizeClass.Unspecified,
    @property:CalfIosOnlyApi val iosVerticalSizeClass: IosSizeClass = IosSizeClass.Unspecified,
    @property:CalfIosOnlyApi val iosCapabilities: IosFoldCapabilities = IosFoldCapabilities.Unavailable,
) {
    /** Whether the fold splits the content into two logical areas. See [FoldInfo.isSeparating]. */
    val isSeparating: Boolean
        get() = fold?.isSeparating == true

    companion object {
        /** No fold, and every iOS value empty. What desktop and web always report. */
        val NoFold: FoldSnapshot = FoldSnapshot()
    }
}

/**
 * The same information as Material3 Adaptive's [Posture], which Compose Multiplatform leaves empty
 * on iOS. Pass it to `WindowAdaptiveInfo(windowSizeClass, windowPosture)` to make the Material3
 * adaptive scaffolds fold-aware there.
 *
 * A closed device reports no hinge, as Jetpack WindowManager does.
 */
@ExperimentalCalfFoldableApi
fun FoldSnapshot.toWindowPosture(): Posture {
    val fold = fold
    if (fold == null || !posture.isOpen) return Posture()

    val isHalfOpen = posture == FoldPosture.HalfOpen
    val hinge = HingeInfo(
        bounds = fold.bounds,
        isFlat = !isHalfOpen,
        isVertical = fold.orientation == FoldOrientation.Vertical,
        isSeparating = fold.isSeparating,
        // Occlusion is an Android-only notion (dual-screen hinges). Use FoldingFeature.occlusionType there.
        isOccluding = false,
    )
    return Posture(
        isTabletop = isHalfOpen && fold.orientation == FoldOrientation.Horizontal,
        hingeList = listOf(hinge),
    )
}

/**
 * Turns iOS's division region into the shared [FoldInfo]. [posture] decides whether the fold
 * separates the content, not [FoldRegion.isActive]: regions lag the hinge by up to a second.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
internal fun foldInfoOfIosRegion(region: FoldRegion?, posture: FoldPosture): FoldInfo? {
    if (region == null) return null
    val orientation =
        if (region.bounds.height >= region.bounds.width) FoldOrientation.Vertical else FoldOrientation.Horizontal
    return FoldInfo(
        bounds = region.reservedBounds,
        orientation = orientation,
        isSeparating = posture == FoldPosture.HalfOpen,
    )
}
