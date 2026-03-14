package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * Internal CompositionLocal used to communicate the native iOS UITabBar height
 * from [AdaptiveNavigationBar] to [AdaptiveScaffold].
 *
 * When [AdaptiveNavigationBar] is used inside an [AdaptiveScaffold]'s `bottomBar`,
 * it measures the native UITabBar height and writes it to this local.
 * The [AdaptiveScaffold] reads this value and adds it to the bottom padding
 * passed to the `content` lambda, so consumers don't need to handle iOS tab bar
 * insets manually.
 */
internal val LocalIosTabBarPadding = compositionLocalOf<MutableState<PaddingValues>> {
    mutableStateOf(PaddingValues())
}
