package com.mohamedrejeb.calf.ui.toggle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * iosX64 fallback: the backdrop library does not support iosX64,
 * so we use a pure Compose approximation instead.
 */
@Composable
actual fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    thumbContent: @Composable (() -> Unit)?,
    enabled: Boolean,
    colors: LiquidGlassSwitchColors,
    interactionSource: MutableInteractionSource,
) {
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val scope = rememberCoroutineScope()

    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val currentChecked by rememberUpdatedState(checked)

    // Total horizontal travel in pixels for the thumb
    val trackWidthPx = with(density) { LiquidGlassSwitchDefaults.TrackWidth.toPx() }
    val thumbWidthPx = with(density) { LiquidGlassSwitchDefaults.ThumbWidth.toPx() }
    val paddingPx = with(density) { LiquidGlassSwitchDefaults.TrackPadding.toPx() }
    val dragWidthPx = trackWidthPx - thumbWidthPx - paddingPx * 2

    // Animated fraction: 0 = unchecked, 1 = checked
    val fraction = remember { Animatable(if (checked) 1f else 0f) }

    // Sync fraction when checked state changes externally
    LaunchedEffect(checked) {
        val target = if (checked) 1f else 0f
        if (fraction.value != target) {
            fraction.animateTo(
                target,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
            )
        }
    }

    var isPressed by remember { mutableStateOf(false) }

    val thumbScaleX by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed && enabled) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
    )
    val thumbScaleY by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
    )

    val trackColor by animateColorAsState(
        targetValue = colors.trackColor(enabled, checked),
    )

    // Convert fraction to BiasAlignment horizontal bias: -1 (unchecked) to 1 (checked)
    val animatedAlignment = fraction.value * 2f - 1f

    Box(
        modifier = modifier
            .semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val press = PressInteraction.Press(it)
                        interactionSource.emit(press)
                        try {
                            awaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))
                        } catch (_: Exception) {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                        isPressed = false
                    },
                    onTap = {
                        currentOnCheckedChange?.invoke(!currentChecked)
                    },
                )
            }
            .pointerInput(enabled, isLtr) {
                if (!enabled) return@pointerInput
                var didDrag = false
                detectHorizontalDragGestures(
                    onDragStart = {
                        isPressed = true
                        didDrag = false
                    },
                    onDragEnd = {
                        isPressed = false
                        if (didDrag) {
                            val newChecked = fraction.value >= 0.5f
                            scope.launch {
                                fraction.animateTo(
                                    if (newChecked) 1f else 0f,
                                    spring(dampingRatio = 0.7f, stiffness = 400f),
                                )
                            }
                            if (newChecked != currentChecked) {
                                currentOnCheckedChange?.invoke(newChecked)
                            }
                        }
                    },
                    onDragCancel = {
                        isPressed = false
                        scope.launch {
                            fraction.animateTo(
                                if (currentChecked) 1f else 0f,
                                spring(dampingRatio = 0.7f, stiffness = 400f),
                            )
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (dragAmount != 0f) didDrag = true
                        val delta = if (isLtr) dragAmount / dragWidthPx else -dragAmount / dragWidthPx
                        scope.launch {
                            fraction.snapTo((fraction.value + delta).coerceIn(0f, 1f))
                        }
                    },
                )
            }
            .graphicsLayer {
                alpha = if (enabled) 1f else LiquidGlassSwitchDefaults.DisabledAlpha
            }
            .wrapContentSize(Alignment.Center)
            .requiredSize(LiquidGlassSwitchDefaults.TrackWidth, LiquidGlassSwitchDefaults.TrackHeight)
            .clip(LiquidGlassSwitchDefaults.TrackShape)
            .background(trackColor)
            .padding(LiquidGlassSwitchDefaults.TrackPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(BiasAlignment(animatedAlignment, 0f))
                .graphicsLayer {
                    scaleX = thumbScaleX
                    scaleY = thumbScaleY
                }
                .shadow(
                    elevation = 4.dp,
                    shape = LiquidGlassSwitchDefaults.TrackShape,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.15f),
                )
                .size(LiquidGlassSwitchDefaults.ThumbWidth, LiquidGlassSwitchDefaults.ThumbHeight)
                .clip(LiquidGlassSwitchDefaults.TrackShape)
                .background(colors.thumbColor(enabled, checked)),
            contentAlignment = Alignment.Center,
        ) {
            thumbContent?.invoke()
        }
    }
}
