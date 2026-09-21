package com.mohamedrejeb.calf.foldable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo

/**
 * Follows Jetpack WindowManager's layout info of the hosting window. Only what both platforms
 * share is read from it; for everything else (occlusion type, several folding features, the
 * hinge angle sensor, rear display) use WindowManager directly.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
internal actual fun rememberPlatformFoldState(): FoldState {
    val context = LocalContext.current
    val state = remember { FoldState() }

    LaunchedEffect(context, state) {
        WindowInfoTracker.getOrCreate(context)
            .windowLayoutInfo(context)
            .collect { layoutInfo -> state.snapshot = layoutInfo.toFoldSnapshot() }
    }

    return state
}

/** Devices with more than one fold are rare enough that the shared model describes the first. */
@OptIn(ExperimentalCalfFoldableApi::class)
private fun WindowLayoutInfo.toFoldSnapshot(): FoldSnapshot {
    val foldingFeature = displayFeatures.firstNotNullOfOrNull { it as? FoldingFeature }
        ?: return FoldSnapshot.NoFold

    return FoldSnapshot(
        posture = foldPostureOfFoldingFeature(
            hasFoldingFeature = true,
            isHalfOpened = foldingFeature.state == FoldingFeature.State.HALF_OPENED,
        ),
        fold = FoldInfo(
            bounds = foldingFeature.bounds.toComposeRect(),
            orientation = when (foldingFeature.orientation) {
                FoldingFeature.Orientation.HORIZONTAL -> FoldOrientation.Horizontal
                else -> FoldOrientation.Vertical
            },
            isSeparating = foldingFeature.isSeparating,
        ),
    )
}
