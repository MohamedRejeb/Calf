package com.mohamedrejeb.calf.ui.toggle

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove

@Composable
actual fun AdaptiveSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    thumbContent: (@Composable () -> Unit)?,
    enabled: Boolean,
    colors: SwitchColors,
    interactionSource: MutableInteractionSource,
) {
    if (isIOS26OrAbove()) {
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            thumbContent = thumbContent,
            enabled = enabled,
            colors = LiquidGlassSwitchDefaults.colors(),
            interactionSource = interactionSource,
        )
    } else {
        CupertinoSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            thumbContent = thumbContent,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
        )
    }
}
