package com.mohamedrejeb.calf.ui.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * A button that uses Material 3's `Button` on Android, Desktop and Web, a Cupertino button on
 * iOS below 26 and a Liquid Glass button on iOS 26+.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 * @param shape The shape of the button. Material only; iOS uses the platform shape.
 * @param colors The container and content colors. On iOS they tint the Cupertino or Liquid Glass
 *   button unless [liquidGlassColors] is set.
 * @param elevation The elevation of the button in its interaction states; `null` draws no shadow.
 *   Material only; ignored on iOS.
 * @param border The border drawn around the button, as on an outlined button. Material only;
 *   ignored on iOS.
 * @param liquidGlassColors The colors of the Liquid Glass button on iOS 26+. Ignored elsewhere.
 * @param contentPadding The padding around the content. Material only; iOS uses the platform
 *   padding.
 * @param interactionSource The [MutableInteractionSource] of the button.
 * @param content The content of the button, typically a `Text`.
 */
@Composable
expect fun AdaptiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    liquidGlassColors: LiquidGlassButtonColors? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
)
