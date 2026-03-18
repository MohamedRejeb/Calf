package com.mohamedrejeb.calf.ui.toggle

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
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
import kotlinx.coroutines.flow.collectLatest

/**
 * A Liquid Glass-style switch/toggle that uses the Backdrop library for real glass effects
 *
 * @param checked Whether the switch is checked
 * @param onCheckedChange Callback when the switch state changes
 * @param modifier The modifier to apply to the switch
 * @param thumbContent Optional content to display inside the thumb
 * @param enabled Whether the switch is enabled
 * @param colors The colors for the switch
 * @param interactionSource The interaction source for this switch
 */
@ExperimentalCalfUiApi
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: LiquidGlassSwitchColors = LiquidGlassSwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()

    val isLightTheme = !isSystemInDarkTheme()
    val accentColor =
        if (isLightTheme) Color(0xFF34C759)
        else Color(0xFF30D158)
    val trackColor =
        if (isLightTheme) Color(0xFF787878).copy(0.2f)
        else Color(0xFF787880).copy(0.36f)

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val dragWidth = with(density) { 20f.dp.toPx() }
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }
    val fraction = remember { mutableFloatStateOf(if (checked) 1f else 0f) }
    val currentChecked by rememberUpdatedState(checked)
    val dampedDragAnimation = remember(animationScope) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction.value,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.5f,
            onDragStarted = {},
            onDragStopped = {
                if (didDrag) {
                    fraction.value = if (targetValue >= 0.5f) 1f else 0f
                    onCheckedChange?.invoke(fraction.value == 1f)
                    didDrag = false
                } else {
                    fraction.value = if (currentChecked) 0f else 1f
                    onCheckedChange?.invoke(fraction.value == 1f)
                }
            },
            onDrag = { change, dragAmount ->
                if (!enabled) return@DampedDragAnimation
                if (!didDrag) {
                    didDrag = dragAmount.x != 0f
                }
                val delta = dragAmount.x / dragWidth
                change.consume()
                fraction.value =
                    if (isLtr) (fraction.value + delta).fastCoerceIn(0f, 1f)
                    else (fraction.value - delta).fastCoerceIn(0f, 1f)
            }
        )
    }
    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { fraction.value }
            .collectLatest { fraction ->
                dampedDragAnimation.updateValue(fraction)
            }
    }
    LaunchedEffect(checked) {
        val target = if (checked) 1f else 0f
        if (target != fraction.value) {
            fraction.value = target
            dampedDragAnimation.animateToValue(target)
        }
    }

    val trackBackdrop = rememberLayerBackdrop()

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
    ) {
        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .clip(Capsule())
                .drawBehind {
                    val fraction = dampedDragAnimation.value
                    drawRect(lerp(trackColor, accentColor, fraction))
                }
                .size(64f.dp, 28f.dp)
        )

        Box(
            Modifier
                .graphicsLayer {
                    val fraction = dampedDragAnimation.value
                    val padding = 2f.dp.toPx()
                    translationX =
                        if (isLtr) lerp(padding, padding + dragWidth, fraction)
                        else lerp(-padding, -(padding + dragWidth), fraction)
                }
                .semantics {
                    role = Role.Switch
                }
                .then(dampedDragAnimation.modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = lerp(2f / 3f, 0.75f, progress)
                            val scaleY = lerp(0f, 0.75f, progress)
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
                            5f.dp.toPx() * progress,
                            10f.dp.toPx() * progress,
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
                        val velocity = dampedDragAnimation.velocity / 50f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(Color.White.copy(alpha = 1f - progress))
                    }
                )
                .size(40f.dp, 24f.dp)
        )
    }
}

/**
 * Colors for [LiquidGlassSwitch].
 */
@Immutable
class LiquidGlassSwitchColors internal constructor(
    val checkedTrackColor: Color,
    val uncheckedTrackColor: Color,
    val checkedThumbColor: Color,
    val uncheckedThumbColor: Color,
    val checkedTrackBorderColor: Color,
    val uncheckedTrackBorderColor: Color,
    val checkedThumbBorderColor: Color,
    val uncheckedThumbBorderColor: Color,
    val disabledCheckedTrackColor: Color,
    val disabledUncheckedTrackColor: Color,
    val disabledCheckedThumbColor: Color,
    val disabledUncheckedThumbColor: Color,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LiquidGlassSwitchColors) return false
        if (checkedTrackColor != other.checkedTrackColor) return false
        if (uncheckedTrackColor != other.uncheckedTrackColor) return false
        if (checkedThumbColor != other.checkedThumbColor) return false
        if (uncheckedThumbColor != other.uncheckedThumbColor) return false
        if (checkedTrackBorderColor != other.checkedTrackBorderColor) return false
        if (uncheckedTrackBorderColor != other.uncheckedTrackBorderColor) return false
        if (checkedThumbBorderColor != other.checkedThumbBorderColor) return false
        if (uncheckedThumbBorderColor != other.uncheckedThumbBorderColor) return false
        if (disabledCheckedTrackColor != other.disabledCheckedTrackColor) return false
        if (disabledUncheckedTrackColor != other.disabledUncheckedTrackColor) return false
        if (disabledCheckedThumbColor != other.disabledCheckedThumbColor) return false
        if (disabledUncheckedThumbColor != other.disabledUncheckedThumbColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = checkedTrackColor.hashCode()
        result = 31 * result + uncheckedTrackColor.hashCode()
        result = 31 * result + checkedThumbColor.hashCode()
        result = 31 * result + uncheckedThumbColor.hashCode()
        result = 31 * result + checkedTrackBorderColor.hashCode()
        result = 31 * result + uncheckedTrackBorderColor.hashCode()
        result = 31 * result + checkedThumbBorderColor.hashCode()
        result = 31 * result + uncheckedThumbBorderColor.hashCode()
        result = 31 * result + disabledCheckedTrackColor.hashCode()
        result = 31 * result + disabledUncheckedTrackColor.hashCode()
        result = 31 * result + disabledCheckedThumbColor.hashCode()
        result = 31 * result + disabledUncheckedThumbColor.hashCode()
        return result
    }
}

@Stable
internal fun LiquidGlassSwitchColors.trackColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedTrackColor else uncheckedTrackColor
    } else {
        if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
    }

@Stable
internal fun LiquidGlassSwitchColors.thumbColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedThumbColor else uncheckedThumbColor
    } else {
        if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
    }

@Stable
internal fun LiquidGlassSwitchColors.trackBorderColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedTrackBorderColor else uncheckedTrackBorderColor
    } else {
        Color.Transparent
    }

@Stable
internal fun LiquidGlassSwitchColors.thumbBorderColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedThumbBorderColor else uncheckedThumbBorderColor
    } else {
        Color.Transparent
    }

/**
 * Defaults for [LiquidGlassSwitch].
 */
object LiquidGlassSwitchDefaults {

    val TrackWidth = 64.dp
    val TrackHeight = 28.dp
    val TrackShape = RoundedCornerShape(50)
    val TrackPadding = 2.dp

    val ThumbWidth = 40.dp
    val ThumbHeight = 24.dp

    /** Drag distance for the thumb (track - thumb - padding*2) */
    val DragWidth = 20.dp

    const val DisabledAlpha = 0.4f

    /** iOS 26 green accent for checked state */
    private val CheckedTrackColor = Color(0xFF34C759)

    /** Glass-like translucent track for unchecked state */
    private val UncheckedTrackColor = Color(0x33787878)

    /** Glass-like translucent white thumb */
    private val ThumbColor = Color(0xCCFFFFFF)

    /** Subtle glass border */
    private val GlassBorder = Color(0x50FFFFFF)

    /** Checked track border with slight green tint */
    private val CheckedTrackBorder = Color(0x3034C759)

    /** Unchecked track border */
    private val UncheckedTrackBorder = Color(0x40FFFFFF)

    /** Thumb border highlight */
    private val ThumbBorder = Color(0x60FFFFFF)

    fun colors(
        checkedTrackColor: Color = CheckedTrackColor,
        uncheckedTrackColor: Color = UncheckedTrackColor,
        checkedThumbColor: Color = ThumbColor,
        uncheckedThumbColor: Color = ThumbColor,
        checkedTrackBorderColor: Color = CheckedTrackBorder,
        uncheckedTrackBorderColor: Color = UncheckedTrackBorder,
        checkedThumbBorderColor: Color = ThumbBorder,
        uncheckedThumbBorderColor: Color = ThumbBorder,
        disabledCheckedTrackColor: Color = checkedTrackColor.copy(alpha = 0.3f),
        disabledUncheckedTrackColor: Color = uncheckedTrackColor.copy(alpha = 0.3f),
        disabledCheckedThumbColor: Color = checkedThumbColor.copy(alpha = 0.5f),
        disabledUncheckedThumbColor: Color = uncheckedThumbColor.copy(alpha = 0.5f),
    ): LiquidGlassSwitchColors = LiquidGlassSwitchColors(
        checkedTrackColor = checkedTrackColor,
        uncheckedTrackColor = uncheckedTrackColor,
        checkedThumbColor = checkedThumbColor,
        uncheckedThumbColor = uncheckedThumbColor,
        checkedTrackBorderColor = checkedTrackBorderColor,
        uncheckedTrackBorderColor = uncheckedTrackBorderColor,
        checkedThumbBorderColor = checkedThumbBorderColor,
        uncheckedThumbBorderColor = uncheckedThumbBorderColor,
        disabledCheckedTrackColor = disabledCheckedTrackColor,
        disabledUncheckedTrackColor = disabledUncheckedTrackColor,
        disabledCheckedThumbColor = disabledCheckedThumbColor,
        disabledUncheckedThumbColor = disabledUncheckedThumbColor,
    )
}
