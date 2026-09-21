package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.calf.navigation.internal.materialListDetail

/**
 * Material actual: Nav3's [NavDisplay], unchanged. Back-stack changes made
 * while a transition is animating behave as they do in any Nav3 app.
 *
 * With [listDetail] the display also gets Material 3's list-detail scene, which
 * takes over while there is room for two panes.
 */
@ExperimentalCalfNavigationApi
@Composable
actual fun AdaptiveNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier,
    onBack: () -> Unit,
    entryDecorators: List<NavEntryDecorator<NavKey>>,
    listDetail: AdaptiveListDetailSettings?,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    if (listDetail == null) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = onBack,
            entryDecorators = entryDecorators,
            entryProvider = entryProvider,
        )
        return
    }

    val material = materialListDetail(backStack, entryProvider)
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = onBack,
        entryDecorators = entryDecorators,
        sceneStrategies = material.sceneStrategies,
        entryProvider = material.entryProvider,
    )
}
