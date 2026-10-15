package com.mohamedrejeb.calf.ui.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove

/**
 * iOS implementation of [AdaptiveButton]: a Liquid Glass button on iOS 26+, a Cupertino button
 * below. [shape], [elevation], [border] and [contentPadding] are Material only and ignored here.
 */
@Composable
actual fun AdaptiveButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    shape: Shape,
    colors: ButtonColors,
    elevation: ButtonElevation?,
    border: BorderStroke?,
    liquidGlassColors: LiquidGlassButtonColors?,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable RowScope.() -> Unit,
) {
    if (isIOS26OrAbove()) {
        LiquidGlassButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = liquidGlassColors ?: LiquidGlassButtonDefaults.filledButtonColors(
                tintColor = colors.containerColor,
                contentColor = colors.contentColor,
                disabledContentColor = colors.disabledContentColor,
            ),
            shape = LiquidGlassButtonDefaults.Shape,
            contentPadding = LiquidGlassButtonDefaults.ContentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    } else {
        CupertinoButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = CupertinoButtonDefaults.filledButtonColors(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor,
                disabledContainerColor = colors.disabledContainerColor,
                disabledContentColor = colors.disabledContentColor,
            ),
            shape = CupertinoButtonDefaults.Shape,
            contentPadding = CupertinoButtonDefaults.ContentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    }
}
