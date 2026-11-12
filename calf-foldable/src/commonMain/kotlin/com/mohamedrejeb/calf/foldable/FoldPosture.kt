package com.mohamedrejeb.calf.foldable

/** How far a foldable device is opened. */
@ExperimentalCalfFoldableApi
enum class FoldPosture {

    /**
     * There is no fold to take into account: the device does not fold, the platform has no fold
     * API, or the window is not on a folding display.
     *
     * Android also reports this while a foldable is closed, because the cover display has no
     * `FoldingFeature`. Only iOS reports [Closed].
     */
    NoFold,

    /** The device is folded shut and the app runs on its cover display. iOS only. */
    Closed,

    /** The device is partly open, like a book or a laptop. */
    HalfOpen,

    /** The device is opened all the way. */
    Flat;

    /** The folding display is in use, so there can be content on both sides of the fold. */
    val isOpen: Boolean
        get() = this == HalfOpen || this == Flat
}

private const val IOS_HINGE_STATUS_CLOSED = 1L
private const val IOS_HINGE_STATUS_PARTIALLY_OPEN = 2L
private const val IOS_HINGE_STATUS_FULLY_OPEN = 3L

/**
 * Maps a `UIHingeStatus` raw value (iOS 27.1). `unknown` (0) and values a newer iOS may add are
 * [FoldPosture.NoFold]: a layout must not split on a status it does not understand.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
internal fun foldPostureOfIosHingeStatus(rawStatus: Long): FoldPosture =
    when (rawStatus) {
        IOS_HINGE_STATUS_CLOSED -> FoldPosture.Closed
        IOS_HINGE_STATUS_PARTIALLY_OPEN -> FoldPosture.HalfOpen
        IOS_HINGE_STATUS_FULLY_OPEN -> FoldPosture.Flat
        else -> FoldPosture.NoFold
    }

/** Maps Jetpack WindowManager's `FoldingFeature.State`, which only knows `HALF_OPENED` and `FLAT`. */
@OptIn(ExperimentalCalfFoldableApi::class)
internal fun foldPostureOfFoldingFeature(hasFoldingFeature: Boolean, isHalfOpened: Boolean): FoldPosture =
    when {
        !hasFoldingFeature -> FoldPosture.NoFold
        isHalfOpened -> FoldPosture.HalfOpen
        else -> FoldPosture.Flat
    }
