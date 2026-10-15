package com.mohamedrejeb.calf.ui.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove

/**
 * iOS implementation of [AdaptiveIconButton]: a Liquid Glass button on iOS 26+, a Cupertino
 * button below. [shape] is Material only and ignored here.
 */
@Composable
actual fun AdaptiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    colors: IconButtonColors,
    shape: Shape,
    liquidGlassColors: LiquidGlassButtonColors?,
    interactionSource: MutableInteractionSource,
    content: @Composable () -> Unit,
) {
    if (isIOS26OrAbove()) {
        LiquidGlassButton(
            onClick = onClick,
            modifier = modifier.size(LiquidGlassButtonDefaults.IconSize),
            enabled = enabled,
            colors = liquidGlassColors ?: LiquidGlassButtonDefaults.plainButtonColors(
                contentColor = colors.contentColor,
                disabledContentColor = colors.disabledContentColor,
            ),
            shape = LiquidGlassButtonDefaults.Shape,
            contentPadding = PaddingValues(),
            interactionSource = interactionSource,
        ) {
            Box(contentAlignment = Alignment.Center) {
                content()
            }
        }
    } else {
        CupertinoButton(
            onClick = onClick,
            modifier = modifier.size(CupertinoButtonDefaults.IconSize),
            enabled = enabled,
            colors = CupertinoButtonDefaults.plainButtonColors(),
            shape = CupertinoButtonDefaults.Shape,
            contentPadding = PaddingValues(),
            interactionSource = interactionSource,
        ) {
            Box(contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}
