package com.mohamedrejeb.calf.ui.utils

import androidx.compose.runtime.staticCompositionLocalOf
import com.kyant.backdrop.Backdrop
import com.mohamedrejeb.calf.ui.button.LiquidGlassButton

/**
 * CompositionLocal to provide a [Backdrop] instance for Liquid Glass buttons.
 *
 * When provided, [LiquidGlassButton] will use the backdrop library's real glass effects
 * (vibrancy, blur, lens) for authentic Liquid Glass rendering.
 */
val LocalBackdrop = staticCompositionLocalOf<Backdrop?> { null }
