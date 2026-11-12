package com.mohamedrejeb.calf.ui.slider

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * A slider that uses Material 3's `Slider` on Android, Desktop and Web, a Cupertino slider on
 * iOS below 26 and a Liquid Glass slider on iOS 26+.
 *
 * @param value The current value of the slider.
 * @param onValueChange Called with the new value while the user drags the slider.
 * @param modifier The modifier to be applied to the slider.
 * @param enabled Whether the slider is enabled.
 * @param valueRange The range of values the slider can take; [value] is coerced into it.
 * @param steps The number of discrete values between the ends of [valueRange]; `0` is continuous.
 * @param onValueChangeFinished Called once the user has finished changing the value.
 * @param colors The colors of the slider. Material only; iOS uses the platform colors.
 * @param interactionSource The [MutableInteractionSource] of the slider.
 * @param thumb A custom thumb, drawn in place of [SliderDefaults.Thumb]; `null` keeps the
 *   default. Material only; ignored on iOS.
 * @param track A custom track, drawn in place of [SliderDefaults.Track]; `null` keeps the
 *   default. Material only; ignored on iOS.
 */
@Composable
expect fun AdaptiveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumb: (@Composable (SliderState) -> Unit)? = null,
    track: (@Composable (SliderState) -> Unit)? = null,
)
