package com.mohamedrejeb.calf.foldable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.uikit.LocalUIViewController

/**
 * Observes the view of the view controller hosting this composition, so bounds are in the same
 * coordinate space as the composition's root. Reports no fold before iOS 27.1.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
internal actual fun rememberPlatformFoldState(): FoldState {
    val hostView = LocalUIViewController.current.view
    val density by rememberUpdatedState(LocalDensity.current.density)
    val observer = remember(hostView) { IosFoldObserver(hostView, density = { density }) }

    DisposableEffect(observer) {
        observer.attach()
        onDispose { observer.detach() }
    }

    return observer.state
}
