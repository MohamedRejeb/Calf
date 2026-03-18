package com.mohamedrejeb.calf.ui.slider

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.utils.DampedDragAnimation
import com.mohamedrejeb.calf.ui.utils.LocalBackdrop

/**
 * A Liquid Glass-style slider that uses the Backdrop library for real glass effects
 *
 * @param value The current value of the slider (within [valueRange])
 * @param onValueChange Called when the value changes during drag or tap
 * @param modifier The modifier to apply to the slider
 * @param enabled Whether the slider is enabled
 * @param valueRange The range of values for the slider
 * @param onValueChangeFinished Called when the user finishes changing the value (drag end or tap)
 * @param colors The colors for the slider
 * @param interactionSource The interaction source for this slider
 */
@ExperimentalCalfUiApi
@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: LiquidGlassSliderColors = LiquidGlassSliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()

    val isLightTheme = !isSystemInDarkTheme()
    val accentColor =
        if (isLightTheme) Color(0xFF0088FF)
        else Color(0xFF0091FF)
    val trackColor =
        if (isLightTheme) Color(0xFF787878).copy(0.2f)
        else Color(0xFF787880).copy(0.36f)

    val trackBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier
            .graphicsLayer {
                alpha = if (enabled) 1f else LiquidGlassSliderDefaults.DisabledAlpha
            }
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        val trackWidth = constraints.maxWidth

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        val currentOnValueChange by rememberUpdatedState(onValueChange)
        val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)
        var didDrag by remember { mutableStateOf(false) }
        val dampedDragAnimation = remember(animationScope) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = value,
                valueRange = valueRange,
                visibilityThreshold = 0.05f,
                initialScale = 1f,
                pressedScale = LiquidGlassSliderDefaults.PressedScale,
                onDragStarted = {},
                onDragStopped = {
                    if (didDrag) {
                        currentOnValueChange(targetValue)
                    }
                    currentOnValueChangeFinished?.invoke()
                },
                onDrag = { change, dragAmount ->
                    if (!enabled) return@DampedDragAnimation
                    if (!didDrag) {
                        didDrag = dragAmount.x != 0f
                    }
                    val delta =
                        (valueRange.endInclusive - valueRange.start) * (dragAmount.x / trackWidth)
                    change.consume()
                    currentOnValueChange(
                        if (isLtr) (targetValue + delta).coerceIn(valueRange)
                        else (targetValue - delta).coerceIn(valueRange)
                    )
                }
            )
        }
        LaunchedEffect(value) {
            if (dampedDragAnimation.targetValue != value) {
                dampedDragAnimation.updateValue(value)
            }
        }

        Box(Modifier.layerBackdrop(trackBackdrop)) {
            Box(
                Modifier
                    .clip(Capsule())
                    .background(trackColor)
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { position ->
                            val delta = (valueRange.endInclusive - valueRange.start) * (position.x / trackWidth)
                            val targetValue =
                                (if (isLtr) valueRange.start + delta
                                else valueRange.endInclusive - delta)
                                    .coerceIn(valueRange)
                            dampedDragAnimation.animateToValue(targetValue)
                            currentOnValueChange(targetValue)
                            currentOnValueChangeFinished?.invoke()
                        }
                    }
                    .height(LiquidGlassSliderDefaults.TrackHeight)
                    .fillMaxWidth()
            )

            Box(
                Modifier
                    .clip(Capsule())
                    .background(accentColor)
                    .height(LiquidGlassSliderDefaults.TrackHeight)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val width = (constraints.maxWidth * dampedDragAnimation.progress).fastRoundToInt()
                        layout(width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    }
            )
        }

        Box(
            Modifier
                .graphicsLayer {
                    translationX =
                        (-size.width / 2f + trackWidth * dampedDragAnimation.progress)
                            .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f) * if (isLtr) 1f else -1f
                }
                .then(dampedDragAnimation.modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = lerp(2f / 3f, 1f, progress)
                            val scaleY = lerp(0f, 1f, progress)
                            scale(scaleX, scaleY) {
                                drawBackdrop()
                            }
                        }
                    ),
                    shape = { Capsule() },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        blur(8f.dp.toPx() * (1f - progress))
                        lens(
                            10f.dp.toPx() * progress,
                            14f.dp.toPx() * progress,
                            chromaticAberration = true
                        )
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = progress
                        )
                    },
                    shadow = {
                        Shadow(
                            radius = 4f.dp,
                            color = Color.Black.copy(alpha = 0.05f)
                        )
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(
                            radius = 4f.dp * progress,
                            alpha = progress
                        )
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(Color.White.copy(alpha = 1f - progress))
                    }
                )
                .size(LiquidGlassSliderDefaults.ThumbWidth, LiquidGlassSliderDefaults.ThumbHeight)
        )
    }
}

/**
 * Color configuration for [LiquidGlassSlider].
 */
@Immutable
class LiquidGlassSliderColors internal constructor(
    val thumbColor: Color,
    val activeTrackColor: Color,
    val trackColor: Color,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LiquidGlassSliderColors) return false
        if (thumbColor != other.thumbColor) return false
        if (activeTrackColor != other.activeTrackColor) return false
        if (trackColor != other.trackColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = thumbColor.hashCode()
        result = 31 * result + activeTrackColor.hashCode()
        result = 31 * result + trackColor.hashCode()
        return result
    }
}

/**
 * Defaults for [LiquidGlassSlider].
 *
 * Dimensions and colors match the reference LiquidSlider from AndroidLiquidGlass:
 * - 6dp track height
 * - 40x24dp capsule thumb
 * - Accent blue active track
 * - Translucent gray track background
 */
object LiquidGlassSliderDefaults {

    /** Track height, matching reference .height(6f.dp) */
    val TrackHeight = 6.dp

    /** Thumb width, matching reference .size(40f.dp, 24f.dp) */
    val ThumbWidth = 40.dp

    /** Thumb height, matching reference .size(40f.dp, 24f.dp) */
    val ThumbHeight = 24.dp

    /** Scale factor when thumb is pressed/dragged, matching reference pressedScale = 1.5f */
    const val PressedScale = 1.5f

    /** Opacity when disabled */
    const val DisabledAlpha = 0.4f

    /** iOS 26 accent blue for active track (light theme) */
    private val AccentColor = Color(0xFF0088FF)

    /** Translucent track background (light theme) */
    private val TrackColor = Color(0x33787878)

    /** Glass-like white thumb */
    private val ThumbColor = Color.White

    fun colors(
        thumbColor: Color = ThumbColor,
        activeTrackColor: Color = AccentColor,
        trackColor: Color = TrackColor,
    ): LiquidGlassSliderColors = LiquidGlassSliderColors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        trackColor = trackColor,
    )
}
