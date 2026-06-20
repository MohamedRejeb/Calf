package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A back-press handler provided by [AdaptiveNavDisplay] so that screen content can
 * pop one entry without holding a reference to the back stack.
 *
 * The default value throws — accessing it outside an [AdaptiveNavDisplay] is a
 * programmer error.
 */
internal val LocalNavBackPress = staticCompositionLocalOf<() -> Unit> {
    error(
        "LocalNavBackPress not provided. Wrap your content in AdaptiveNavDisplay " +
            "or provide LocalNavBackPress explicitly.",
    )
}
