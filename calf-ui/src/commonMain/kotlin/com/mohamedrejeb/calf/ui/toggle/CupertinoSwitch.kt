package com.mohamedrejeb.calf.ui.toggle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun CupertinoSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: @Composable() (() -> Unit)? = null,
    colors: SwitchColors = SwitchDefaults.colors(),
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedAspectRatio by animateFloatAsState(if (isPressed) 1.25f else 1f)
    val animatedBackground by animateColorAsState(colors.trackColor(enabled, checked))
    val animatedAlignment by animateFloatAsState(if (checked) 1f else -1f)

    Column(
        modifier
            .toggleable(
                value = checked,
                onValueChange = {
                    onCheckedChange?.invoke(it)
                },
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null
            )
            .wrapContentSize(Alignment.Center)
            .requiredSize(51.dp, 31.dp)
            .clip(CircleShape)
            .background(animatedBackground)
            .padding(2.dp),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .clip(CircleShape)
                .aspectRatio(animatedAspectRatio)
                .background(colors.thumbColor(enabled, checked))
                .align(BiasAlignment.Horizontal(animatedAlignment))
        ) {
            thumbContent?.invoke()
        }
    }
}

@Stable
internal fun SwitchColors.trackColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedTrackColor else uncheckedTrackColor
    } else {
        if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
    }

@Stable
internal fun SwitchColors.thumbColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedThumbColor else uncheckedThumbColor
    } else {
        if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
    }