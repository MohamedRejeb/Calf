package com.mohamedrejeb.calf.ui.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle

/**
 * iOS-style Cupertino button implemented in pure Compose.
 *
 * Uses opacity press feedback instead of ripple, matching native iOS button behavior.
 *
 * @param onClick Called when the button is clicked
 * @param modifier The modifier to apply to the button
 * @param enabled Whether the button is enabled
 * @param colors The colors for the button
 * @param shape The shape of the button
 * @param contentPadding The padding applied to the content
 * @param interactionSource The interaction source for this button
 * @param content The content of the button
 */
@Composable
fun CupertinoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CupertinoButtonColors = CupertinoButtonDefaults.filledButtonColors(),
    shape: Shape = CupertinoButtonDefaults.Shape,
    contentPadding: PaddingValues = CupertinoButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    val targetAlpha = when {
        !enabled -> CupertinoButtonDefaults.DisabledAlpha
        isPressed -> CupertinoButtonDefaults.PressedAlpha
        else -> 1f
    }

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = if (isPressed) 10 else 250),
    )

    ProvideTextStyle(
        value = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = modifier
                    .alpha(alpha)
                    .semantics { role = Role.Button }
                    .clip(shape)
                    .background(color = containerColor, shape = shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick,
                    )
                    .defaultMinSize(
                        minWidth = CupertinoButtonDefaults.MinWidth,
                        minHeight = CupertinoButtonDefaults.MinHeight,
                    )
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/**
 * Color configuration for [CupertinoButton].
 */
@Immutable
data class CupertinoButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)

/**
 * Defaults for [CupertinoButton].
 */
object CupertinoButtonDefaults {

    /** iOS system blue */
    val SystemBlue = Color(0xFF007AFF)

    /** Capsule shape (fully rounded corners) */
    val Shape: Shape = RoundedCornerShape(50)

    /** Default content padding for filled/bordered buttons */
    val ContentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

    /** Default content padding for plain (text) buttons */
    val PlainContentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)

    /** Minimum width for buttons */
    val MinWidth = 44.dp

    /** Minimum height for buttons */
    val MinHeight = 44.dp

    /** Icon button size */
    val IconSize = 44.dp

    /** Opacity when pressed */
    const val PressedAlpha = 0.33f

    /** Opacity when disabled */
    const val DisabledAlpha = 0.4f

    /**
     * Filled button colors (borderedProminent style).
     * Solid blue background with white text.
     */
    fun filledButtonColors(
        containerColor: Color = SystemBlue,
        contentColor: Color = Color.White,
        disabledContainerColor: Color = SystemBlue.copy(alpha = 0.3f),
        disabledContentColor: Color = Color.White.copy(alpha = 0.5f),
    ): CupertinoButtonColors = CupertinoButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    /**
     * Bordered/tinted button colors (bordered style).
     * Tinted background with tinted text.
     */
    fun borderedButtonColors(
        containerColor: Color = SystemBlue.copy(alpha = 0.15f),
        contentColor: Color = SystemBlue,
        disabledContainerColor: Color = Color.Gray.copy(alpha = 0.1f),
        disabledContentColor: Color = Color.Gray.copy(alpha = 0.5f),
    ): CupertinoButtonColors = CupertinoButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    /**
     * Plain/borderless button colors (plain style).
     * No background, tinted text, opacity changes on press.
     */
    fun plainButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = SystemBlue,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = Color.Gray.copy(alpha = 0.5f),
    ): CupertinoButtonColors = CupertinoButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )
}
