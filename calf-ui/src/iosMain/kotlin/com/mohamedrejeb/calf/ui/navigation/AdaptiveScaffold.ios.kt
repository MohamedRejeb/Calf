package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    val nativeBarInsetsState = remember { mutableStateOf(IosNativeBarInsets()) }

    CompositionLocalProvider(
        LocalIosNativeBarInsets provides nativeBarInsetsState,
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