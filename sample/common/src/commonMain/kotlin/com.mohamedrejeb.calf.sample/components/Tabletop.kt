package com.mohamedrejeb.calf.sample.components

import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldOrientation
import com.mohamedrejeb.calf.foldable.FoldPanes
import com.mohamedrejeb.calf.foldable.FoldPosture

/**
 * The two panes of a tabletop posture: the device is half open and the fold runs across the
 * window, so there is content above the fold and controls below it. `null` in every other pose,
 * including a half-open book (vertical fold) and a flat device.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
internal fun tabletopPanes(posture: FoldPosture, panes: FoldPanes): FoldPanes? =
    panes.takeIf {
        posture == FoldPosture.HalfOpen && it.count == 2 && it.orientation == FoldOrientation.Horizontal
    }
