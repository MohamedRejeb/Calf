package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * Internal CompositionLocal used to communicate the native iOS UINavigationBar height
 * from [AdaptiveTopBar] to [AdaptiveScaffold].
 *
 * When [AdaptiveTopBar] is used inside an [AdaptiveScaffold]'s `topBar`,
 * it measures the native UINavigationBar height and writes it to this local.
 * The [AdaptiveScaffold] reads this value and adds it to the top padding
 * passed to the `content` lambda, so consumers don't need to handle iOS navigation bar
 * insets manually.
 */
internal val LocalIosTopBarPadding = compositionLocalOf<MutableState<PaddingValues>> {
    mutableStateOf(PaddingValues())
}
