package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

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
    val iosTabBarPaddingState = remember { mutableStateOf(PaddingValues()) }

    CompositionLocalProvider(LocalIosTabBarPadding provides iosTabBarPaddingState) {
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

            // Merge scaffold padding with the native iOS tab bar padding.
            // The native UITabBar height replaces scaffold's bottom padding
            // when present, since the tab bar is added as a native UIKit subview
            // and is not measured by the Compose Scaffold layout.
            val iosBottomPadding = iosTabBarPadding.calculateBottomPadding()
            val mergedPadding = PaddingValues(
                start = scaffoldPadding.calculateStartPadding(layoutDirection),
                top = scaffoldPadding.calculateTopPadding(),
                end = scaffoldPadding.calculateEndPadding(layoutDirection),
                bottom = maxOf(
                    scaffoldPadding.calculateBottomPadding(),
                    iosBottomPadding,
                ),
            )

            content(mergedPadding)
        }
    }
}