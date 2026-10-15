package com.mohamedrejeb.calf.ui.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * An icon button that uses Material 3's `IconButton` on Android, Desktop and Web, a Cupertino
 * icon button on iOS below 26 and a Liquid Glass icon button on iOS 26+.
 *
 * @param onClick Called when the icon button is clicked.
 * @param modifier The modifier to be applied to the icon button.
 * @param enabled Whether the icon button is enabled.
 * @param colors The container and content colors. On iOS only the content colors tint the
 *   Liquid Glass button unless [liquidGlassColors] is set.
 * @param shape The shape of the container and of the ripple. Material only; ignored on iOS.
 * @param liquidGlassColors The colors of the Liquid Glass button on iOS 26+. Ignored elsewhere.
 * @param interactionSource The [MutableInteractionSource] of the icon button.
 * @param content The content of the icon button, typically an `Icon`.
 */
@Composable
expect fun AdaptiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    shape: Shape = IconButtonDefaults.standardShape,
    liquidGlassColors: LiquidGlassButtonColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
)
