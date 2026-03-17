package com.mohamedrejeb.calf.ui.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * CompositionLocal to provide a [Backdrop] instance for Liquid Glass buttons.
 *
 * When provided, [LiquidGlassButton] will use the backdrop library's real glass effects
 * (vibrancy, blur, lens) for authentic Liquid Glass rendering.
 */
val LocalBackdrop = staticCompositionLocalOf<Backdrop?> { null }

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
    val backdrop = LocalBackdrop.current
    val isPressed by interactionSource.collectIsPressedAsState()

    // Reference scales UP on press: lerp(1f, 1f + 4dp/height, progress) ≈ 1.08 for 48dp
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) LiquidGlassButtonDefaults.PressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 300f,
        ),
    )

    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    ProvideTextStyle(
        value = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            val backdropModifier = if (backdrop != null) {
                // Real glass effects using the Backdrop library
                Modifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(50) },
                    effects = {
                        vibrancy()
                        blur(2f.dp.toPx())
                        lens(12f.dp.toPx(), 24f.dp.toPx())
                    },
                    layerBlock = {
                        scaleX = scale
                        scaleY = scale
                        alpha = if (enabled) 1f else LiquidGlassButtonDefaults.DisabledAlpha
                    },
                    onDrawSurface = {
                        val tint = colors.tintColor
                        if (tint.isSpecified) {
                            drawRect(tint, blendMode = BlendMode.Hue)
                            drawRect(tint.copy(alpha = 0.75f))
                        }
                        val surface = colors.surfaceColor
                        if (surface.isSpecified) {
                            drawRect(surface)
                        }
                    },
                )
            } else {
                // Fallback: translucent background when no Backdrop is provided
                val containerColor = if (enabled) {
                    colors.fallbackContainerColor
                } else {
                    colors.fallbackDisabledContainerColor
                }
                Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = if (enabled) 1f else LiquidGlassButtonDefaults.DisabledAlpha
                    }
                    .clip(shape)
                    .background(color = containerColor, shape = shape)
            }

            Row(
                modifier = modifier
                    .then(backdropModifier)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onClick,
                    )
                    .height(LiquidGlassButtonDefaults.Height)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}
