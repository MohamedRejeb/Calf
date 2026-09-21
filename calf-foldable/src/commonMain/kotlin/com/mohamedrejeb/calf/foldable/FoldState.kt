package com.mohamedrejeb.calf.foldable

import androidx.compose.material3.adaptive.Posture
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * What an app needs to know about the fold: the posture, where the fold is, and how many panes
 * fit. Get one with [rememberFoldState].
 *
 * Every property except [iosHingeAngle] is Compose state that changes only when the posture or a
 * region changes, a few times per fold. The hinge angle changes on every frame while the device
 * moves, so it is a [Flow] and never causes a recomposition by itself.
 *
 * Members that start with `ios` only have a value on iOS ([CalfIosOnlyApi]). Everything else is
 * backed by Jetpack WindowManager on Android and by UIKit on iOS. Desktop and web report
 * [FoldSnapshot.NoFold].
 */
@ExperimentalCalfFoldableApi
@Stable
class FoldState internal constructor(
    initialSnapshot: FoldSnapshot,
    iosHingeAngle: Flow<Float>,
    private val iosDebugDumpProvider: (FoldSnapshot) -> String,
) {
    /** All current values as one immutable object. */
    var snapshot: FoldSnapshot by mutableStateOf(initialSnapshot)
        internal set

    /** How far the device is opened. */
    val posture: FoldPosture
        get() = snapshot.posture

    /** The fold of the display the window is on, `null` when there is none. */
    val fold: FoldInfo?
        get() = snapshot.fold

    /** Whether the fold splits the content into two logical areas. See [FoldInfo.isSeparating]. */
    val isSeparating: Boolean
        get() = snapshot.isSeparating

    /**
     * How many panes fit in a window of [windowSize] pixels, and where they go. See
     * [calculateFoldPanes], which this calls with the current values; on iOS that includes the
     * margins UIKit asks to keep clear around a separating fold.
     */
    @OptIn(CalfIosOnlyApi::class)
    fun calculatePanes(windowSize: Size, minPaneSize: Float = 0f): FoldPanes =
        calculateFoldPanes(
            windowSize = windowSize,
            posture = snapshot.posture,
            fold = snapshot.fold,
            foldMargins = snapshot.iosFoldRegion?.margins ?: FoldMargins.Zero,
            minPaneSize = minPaneSize,
        )

    /** See [FoldSnapshot.toWindowPosture]. */
    fun toWindowPosture(): Posture = snapshot.toWindowPosture()

    /**
     * iOS only. The fold as UIKit reports it: bounds including margins, the margins, and whether
     * it is active. `null` on other platforms, without a fold, or when the app was not built with
     * the iOS 27.1 SDK.
     */
    @CalfIosOnlyApi
    val iosFoldRegion: FoldRegion?
        get() = snapshot.iosFoldRegion

    /**
     * iOS only. Areas where something covers the display, such as the camera. Empty on other
     * platforms. On Android read `WindowInsets.displayCutout`.
     */
    @CalfIosOnlyApi
    val iosOcclusionRegions: List<FoldRegion>
        get() = snapshot.iosOcclusionRegions

    /** iOS only. [IosSizeClass.Unspecified] on other platforms. */
    @CalfIosOnlyApi
    val iosHorizontalSizeClass: IosSizeClass
        get() = snapshot.iosHorizontalSizeClass

    /** iOS only. [IosSizeClass.Unspecified] on other platforms. */
    @CalfIosOnlyApi
    val iosVerticalSizeClass: IosSizeClass
        get() = snapshot.iosVerticalSizeClass

    /** iOS only. [IosFoldCapabilities.Unavailable] on other platforms. */
    @CalfIosOnlyApi
    val iosCapabilities: IosFoldCapabilities
        get() = snapshot.iosCapabilities

    /**
     * iOS only. The hinge angle in degrees, 0 (shut) to 180 (flat), for effects that follow the
     * hinge frame by frame. It replays the latest angle to a new collector and never emits on
     * other platforms (on Android use `Sensor.TYPE_HINGE_ANGLE`).
     *
     * Lay out from [posture], not from this: iOS turns the posture to [FoldPosture.Closed] well
     * before the angle reaches 0.
     */
    @CalfIosOnlyApi
    val iosHingeAngle: Flow<Float> = iosHingeAngle

    /**
     * iOS only. A multi-line description of what the running iOS exposes and currently reports,
     * meant for bug reports from devices you do not have at hand.
     */
    @CalfIosOnlyApi
    fun iosDebugDump(): String = iosDebugDumpProvider(snapshot)
}

/**
 * A [FoldState] with fixed values, for previews and tests. Provide it through
 * [LocalFoldStateOverride] to make [rememberFoldState] return it.
 */
@ExperimentalCalfFoldableApi
fun FoldState(
    snapshot: FoldSnapshot = FoldSnapshot.NoFold,
    iosHingeAngle: Flow<Float> = emptyFlow(),
): FoldState =
    FoldState(
        initialSnapshot = snapshot,
        iosHingeAngle = iosHingeAngle,
        iosDebugDumpProvider = { current -> "Calf FoldState with fixed values, not reading the platform.\n$current" },
    )
