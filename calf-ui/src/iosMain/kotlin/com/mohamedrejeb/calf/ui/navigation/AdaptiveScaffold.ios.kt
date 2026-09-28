package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.Density
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.utils.LocalBackdrop

@ExperimentalCalfUiApi
@Composable
actual fun AdaptiveScaffold(
    modifier: Modifier,
    topBar: @Composable (() -> Unit),
    bottomBar: @Composable (() -> Unit),
    snackbarHost: @Composable (() -> Unit),
    floatingActionButton: @Composable (() -> Unit),
    floatingActionButtonPosition: FabPosition,
    containerColor: Color,
    contentColor: Color,
    contentWindowInsets: WindowInsets,
    content: @Composable ((PaddingValues) -> Unit)
) {
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val host = LocalAdaptiveBarHost.current?.takeIf { it.isHostSurface(LocalUIViewController.current) }

    CompositionLocalProvider(LocalBackdrop provides backdrop) {
        if (host != null) {
            HostedScaffoldLayout(
                host = host,
                modifier = modifier,
                topBar = topBar,
                bottomBar = bottomBar,
                snackbarHost = snackbarHost,
                floatingActionButton = floatingActionButton,
                floatingActionButtonPosition = floatingActionButtonPosition,
                containerColor = containerColor,
                contentColor = contentColor,
                contentWindowInsets = contentWindowInsets,
                content = content,
            )
        } else {
            StandaloneScaffoldLayout(
                modifier = modifier,
                topBar = topBar,
                bottomBar = bottomBar,
                snackbarHost = snackbarHost,
                floatingActionButton = floatingActionButton,
                floatingActionButtonPosition = floatingActionButtonPosition,
                containerColor = containerColor,
                contentColor = contentColor,
                contentWindowInsets = contentWindowInsets,
                content = content,
            )
        }
    }
}

/** No bar host: the bars are standalone UIKit views on top of the Compose surface. */
@Composable
private fun StandaloneScaffoldLayout(
    modifier: Modifier,
    topBar: @Composable (() -> Unit),
    bottomBar: @Composable (() -> Unit),
    snackbarHost: @Composable (() -> Unit),
    floatingActionButton: @Composable (() -> Unit),
    floatingActionButtonPosition: FabPosition,
    containerColor: Color,
    contentColor: Color,
    contentWindowInsets: WindowInsets,
    content: @Composable ((PaddingValues) -> Unit)
) {
    val nativeBarInsetsState = remember { mutableStateOf(IosNativeBarInsets()) }

    CompositionLocalProvider(
        LocalIosNativeBarInsets provides nativeBarInsetsState,
        // A sheet's content re-applies its parent's locals: its bars must not reach a hosted
        // scaffold of the screen below.
        LocalHostedScaffold provides null,
    ) {
        Scaffold(
            modifier = modifier,
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            containerColor = containerColor,
            contentColor = contentColor,
            contentWindowInsets = contentWindowInsets,
        ) { scaffoldPadding ->
            // The native UINavigationBar / UITabBar are UIKit subviews the Compose Scaffold never
            // measures, so on each of the four edges the larger of the two claims wins.
            // Not animated: UIKit moves the bars on its own clock (rotation, fold), and a padding
            // that eases in on Compose's clock leaves the content under the bar meanwhile.
            val mergedPadding = scaffoldPadding.withNativeBarInsets(
                insets = nativeBarInsetsState.value.total,
                layoutDirection = LocalLayoutDirection.current,
            )

            content(mergedPadding)
        }
    }
}

/**
 * Inside an [AdaptiveBarHostUIViewController]: the bars in [topBar] and [bottomBar] publish to the
 * host's containers instead of drawing (see [HostedScaffold]), and the content is padded by what
 * the containers' bars leave free, as far as it reaches this scaffold.
 */
@Composable
private fun HostedScaffoldLayout(
    host: BarHost,
    modifier: Modifier,
    topBar: @Composable (() -> Unit),
    bottomBar: @Composable (() -> Unit),
    snackbarHost: @Composable (() -> Unit),
    floatingActionButton: @Composable (() -> Unit),
    floatingActionButtonPosition: FabPosition,
    containerColor: Color,
    contentColor: Color,
    contentWindowInsets: WindowInsets,
    content: @Composable ((PaddingValues) -> Unit)
) {
    val parent = LocalHostedScaffold.current?.takeIf { it.host === host }
    val scaffold = remember(host, parent) { HostedScaffold(host, parent) }
    val density = LocalDensity.current
    var boxOffsets by remember { mutableStateOf(IosBarEdgeInsets.Zero) }
    val hostInsets = host.contentInsetsOf(scaffold)
    // A bar the host does not take over (none today) still draws itself and claims its room here.
    val nativeBarInsetsState = remember { mutableStateOf(IosNativeBarInsets()) }

    CompositionLocalProvider(
        LocalHostedScaffold provides scaffold,
        LocalIosNativeBarInsets provides nativeBarInsetsState,
    ) {
        Scaffold(
            modifier = modifier
                .onPlaced { boxOffsets = it.offsetsInRoot(density) }
                .nestedScroll(scaffold.scrollTracker),
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            containerColor = containerColor,
            contentColor = contentColor,
            // The host's insets replace the system ones: they include the containers' bars, and a
            // screen that lost the bars keeps its own (Compose's follow the container live).
            contentWindowInsets =
                if (hostInsets != null) contentWindowInsets.exclude(HostCoveredInsets) else contentWindowInsets,
        ) { scaffoldPadding ->
            val padding = scaffoldPadding.withNativeBarInsets(
                insets = (hostInsets?.reaching(boxOffsets) ?: IosBarEdgeInsets.Zero)
                    .union(nativeBarInsetsState.value.total),
                layoutDirection = LocalLayoutDirection.current,
            )
            content(padding)
        }
    }
}

private val HostCoveredInsets: WindowInsets
    @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

/**
 * How far this layout's edges are from the Compose surface's edges. An axis on which the layout
 * is as large as the surface counts as touching both edges: a full-screen scaffold that slides in
 * during a transition keeps its padding instead of recomputing it every frame.
 */
private fun LayoutCoordinates.offsetsInRoot(density: Density): IosBarEdgeInsets {
    val rootSize = findRootCoordinates().size
    val position = positionInRoot()
    val fillsWidth = size.width >= rootSize.width
    val fillsHeight = size.height >= rootSize.height
    return with(density) {
        IosBarEdgeInsets(
            left = if (fillsWidth) 0f.toDp() else position.x.toDp(),
            top = if (fillsHeight) 0f.toDp() else position.y.toDp(),
            right = if (fillsWidth) 0f.toDp() else (rootSize.width - position.x - size.width).toDp(),
            bottom = if (fillsHeight) 0f.toDp() else (rootSize.height - position.y - size.height).toDp(),
        )
    }
}
