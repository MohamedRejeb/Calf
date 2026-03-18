package com.mohamedrejeb.calf.ui.slider

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SliderColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove

@Composable
actual fun AdaptiveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChangeFinished: (() -> Unit)?,
    colors: SliderColors,
    interactionSource: MutableInteractionSource,
) {
    if (isIOS26OrAbove()) {
        LiquidGlassSlider(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
            colors = LiquidGlassSliderDefaults.colors(),
            interactionSource = interactionSource,
        )
    } else {
        CupertinoSlider(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            interactionSource = interactionSource,
        )
    }
}
