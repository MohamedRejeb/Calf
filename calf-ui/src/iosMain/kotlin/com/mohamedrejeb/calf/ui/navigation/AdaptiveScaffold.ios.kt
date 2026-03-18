package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
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
    val iosTabBarPaddingState = remember { mutableStateOf(PaddingValues()) }
    val iosTopBarPaddingState = remember { mutableStateOf(PaddingValues()) }

    CompositionLocalProvider(
        LocalIosTabBarPadding provides iosTabBarPaddingState,
        LocalIosTopBarPadding provides iosTopBarPaddingState,
        LocalBackdrop provides backdrop
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
            val iosTabBarPadding = iosTabBarPaddingState.value
            val layoutDirection = LocalLayoutDirection.current

            // Merge scaffold padding with the native iOS bar padding.
            // The native UITabBar/UINavigationBar heights replace scaffold's
            // bottom/top padding when present, since the bars are added as
            // native UIKit subviews and are not measured by the Compose Scaffold layout.
            val iosTopBarPadding = iosTopBarPaddingState.value
            val iosBottomPadding = iosTabBarPadding.calculateBottomPadding()
            val iosTopPadding = iosTopBarPadding.calculateTopPadding()

            val animatedTopPadding by animateDpAsState(
                targetValue = maxOf(scaffoldPadding.calculateTopPadding(), iosTopPadding),
            )
            val animatedBottomPadding by animateDpAsState(
                targetValue = maxOf(scaffoldPadding.calculateBottomPadding(), iosBottomPadding),
            )

            val mergedPadding = PaddingValues(
                start = scaffoldPadding.calculateStartPadding(layoutDirection),
                top = animatedTopPadding,
                end = scaffoldPadding.calculateEndPadding(layoutDirection),
                bottom = animatedBottomPadding,
            )

            content(mergedPadding)
        }
    }
}