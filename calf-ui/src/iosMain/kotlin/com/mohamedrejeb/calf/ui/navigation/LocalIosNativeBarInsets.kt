package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * Internal CompositionLocal through which the native iOS bars ([AdaptiveTopBar],
 * [AdaptiveNavigationBar]) tell their [AdaptiveScaffold] how much room they claim.
 *
 * The bars are UIKit subviews the Compose scaffold never measures. Each bar publishes its own
 * contribution under its own key (see [IosNativeBarInsets]) and the scaffold merges the total
 * into the padding passed to its `content` lambda, on all four edges, so consumers don't need
 * to handle the native bars manually.
 *
 * Outside an [AdaptiveScaffold] the default state is written to and never read.
 */
internal val LocalIosNativeBarInsets = compositionLocalOf<MutableState<IosNativeBarInsets>> {
    mutableStateOf(IosNativeBarInsets())
}
