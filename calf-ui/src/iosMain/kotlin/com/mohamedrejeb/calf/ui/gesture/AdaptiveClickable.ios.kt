package com.mohamedrejeb.calf.ui.gesture

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role

@Composable
actual fun Modifier.adaptiveClickable(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean,
    onClickLabel: String?,
    role: Role?,
    shape: Shape,
    onClick: () -> Unit
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = if (isPressed) IOS_BUTTON_SCALE_WHEN_PRESSED else 1f
    val scale by animateFloatAsState(targetValue = targetScale)

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick
        )
}

private const val IOS_BUTTON_SCALE_WHEN_PRESSED = 0.96f
