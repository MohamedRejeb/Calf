package com.mohamedrejeb.calf.ui.gesture

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
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
): Modifier =
    this
        .clip(shape)
        .clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick
        )