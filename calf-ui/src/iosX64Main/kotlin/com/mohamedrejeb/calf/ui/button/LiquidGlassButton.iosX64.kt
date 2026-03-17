package com.mohamedrejeb.calf.ui.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * iosX64 fallback: the backdrop library does not support iosX64,
 * so we delegate to [CupertinoButton] instead.
 */
@Composable
actual fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    colors: LiquidGlassButtonColors,
    shape: Shape,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable RowScope.() -> Unit,
) {
    CupertinoButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = CupertinoButtonDefaults.filledButtonColors(
            containerColor = colors.fallbackContainerColor,
            contentColor = colors.contentColor,
            disabledContainerColor = colors.fallbackDisabledContainerColor,
            disabledContentColor = colors.disabledContentColor,
        ),
        shape = shape,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}
