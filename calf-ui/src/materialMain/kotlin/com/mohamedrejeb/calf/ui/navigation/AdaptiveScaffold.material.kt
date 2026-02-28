package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        content = content,
    )
}