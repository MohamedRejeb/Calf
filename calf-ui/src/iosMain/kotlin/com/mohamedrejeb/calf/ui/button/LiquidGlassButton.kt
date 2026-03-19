package com.mohamedrejeb.calf.ui.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.utils.InteractiveHighlight
import com.mohamedrejeb.calf.ui.utils.LocalBackdrop
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/**
 * A Liquid Glass button that uses the Backdrop library for real glass effects
 *
 * @param onClick Called when the button is clicked
 * @param modifier The modifier to apply to the button
 * @param enabled Whether the button is enabled
 * @param colors The colors for the button
 * @param shape The shape of the button (Capsule by default, matching reference)
 * @param contentPadding The padding applied to the content
 * @param interactionSource The interaction source for this button
 * @param content The content of the button
 */
@ExperimentalCalfUiApi
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: LiquidGlassButtonColors = LiquidGlassButtonDefaults.filledButtonColors(),
    shape: Shape = LiquidGlassButtonDefaults.Shape,
    contentPadding: PaddingValues = LiquidGlassButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val animationScope = rememberCoroutineScope()

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }

    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    ProvideTextStyle(
        value = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
                modifier = modifier
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(2.dp.toPx())
                            lens(12.dp.toPx(), 24.dp.toPx())
                        },
                        layerBlock = if (enabled) {
                            {
                                val progress = interactiveHighlight.pressProgress
                                val scale = lerp(1f, 1f + 4.dp.toPx() / size.height, progress)

                                val maxOffset = size.minDimension
                                val initialDerivative = 0.05f
                                val offset = interactiveHighlight.offset
                                translationX =
                                    maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                                translationY =
                                    maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                                val maxDragScale = 4.dp.toPx() / size.height
                                val offsetAngle = atan2(offset.y, offset.x)
                                scaleX =
                                    scale +
                                            maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                            (size.width / size.height).fastCoerceAtMost(1f)
                                scaleY =
                                    scale +
                                            maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                            (size.height / size.width).fastCoerceAtMost(1f)
                            }
                        } else {
                            {
                                alpha = LiquidGlassButtonDefaults.DisabledAlpha
                            }
                        },
                        onDrawSurface = {
                            if (enabled) {
                                val tint = colors.tintColor
                                if (tint.isSpecified) {
                                    drawRect(tint, blendMode = BlendMode.Hue)
                                    drawRect(tint.copy(alpha = 0.75f))
                                }
                                val surface = colors.surfaceColor
                                if (surface.isSpecified) {
                                    drawRect(surface)
                                }
                            }
                        },
                    )
                    .then(
                        if (enabled)
                            Modifier
                                .then(interactiveHighlight.modifier)
                                .then(interactiveHighlight.gestureModifier)
                        else
                            Modifier
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onClick,
                    )
                    .height(LiquidGlassButtonDefaults.Height)
                    .padding(contentPadding)
            )
        }
    }
}


/**
 * Defaults for [LiquidGlassButton].
 *
 * Dimensions and behavior match the reference LiquidButton from AndroidLiquidGlass:
 * - Capsule shape
 * - 48dp height
 * - 16dp horizontal padding
 * - 8dp content spacing
 * - Scale-up press animation (~1.08x)
 * - drawBackdrop with vibrancy, blur(2dp), lens(12dp, 24dp) effects
 */
object LiquidGlassButtonDefaults {

    /** Capsule shape */
    val Shape: Shape = Capsule()

    /** Fixed button height, matching reference .height(48f.dp) */
    val Height = 48.dp

    /** Default content padding, matching reference .padding(horizontal = 16f.dp) */
    val ContentPadding = PaddingValues(horizontal = 16.dp)

    /** Default content padding for plain (text) buttons */
    val PlainContentPadding = PaddingValues(horizontal = 8.dp)

    /** Icon button size */
    val IconSize = 48.dp

    /**
     * Scale factor when pressed.
     * Reference: lerp(1f, 1f + 4dp/height, progress) ≈ 1.083 for a 48dp button.
     */
    const val PressedScale = 1.08f

    /** Opacity when disabled */
    const val DisabledAlpha = 0.4f

    /**
     * Filled liquid glass button colors.
     *
     * Pure glass with no tint — uses only vibrancy, blur, and lens effects.
     * Matching the default reference LiquidButton with no tint/surfaceColor.
     */
    fun filledButtonColors(
        tintColor: Color = Color.Unspecified,
        surfaceColor: Color = Color.Unspecified,
        contentColor: Color = Color.White,
        disabledContentColor: Color = Color.White.copy(alpha = 0.4f),
    ): LiquidGlassButtonColors = LiquidGlassButtonColors(
        tintColor = tintColor,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
    )

    /**
     * Bordered/tinted liquid glass button colors.
     *
     * Applies a tint using `BlendMode.Hue`, matching the reference's tinted icon button pattern:
     * `drawRect(tint, blendMode = BlendMode.Hue); drawRect(tint.copy(alpha = 0.75f))`
     */
    fun borderedButtonColors(
        tintColor: Color = Color(0xFF007AFF),
        surfaceColor: Color = Color.Unspecified,
        contentColor: Color = Color(0xFF007AFF),
        disabledContentColor: Color = Color(0xFF007AFF).copy(alpha = 0.4f),
    ): LiquidGlassButtonColors = LiquidGlassButtonColors(
        tintColor = tintColor,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
    )

    /**
     * Plain liquid glass button colors.
     * No glass background, content only, with subtle scale on press.
     */
    fun plainButtonColors(
        tintColor: Color = Color.Unspecified,
        surfaceColor: Color = Color.Unspecified,
        contentColor: Color = Color(0xFF007AFF),
        disabledContentColor: Color = Color(0xFF007AFF).copy(alpha = 0.4f),
    ): LiquidGlassButtonColors = LiquidGlassButtonColors(
        tintColor = tintColor,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
    )
}
