package com.mohamedrejeb.calf.ui.slider

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * A Cupertino-style slider for iOS < 26.
 *
 * Based on the compose-cupertino library implementation:
 * https://github.com/alexzhirkevich/compose-cupertino/blob/master/cupertino/src/commonMain/kotlin/io/github/alexzhirkevich/cupertino/CupertinoSlider.kt
 *
 * @param value current value of the slider. If outside of [valueRange] provided, value will be
 * coerced to this range.
 * @param onValueChange callback in which value should be updated
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled controls the enabled state of this slider.
 * @param valueRange range of values that this slider can take.
 * @param steps if greater than 0, specifies the amount of discrete allowable values.
 * @param onValueChangeFinished called when value change has ended.
 * @param colors [CupertinoSliderColors] that will be used to resolve the colors.
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s.
 */
@Composable
internal fun CupertinoSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: CupertinoSliderColors = CupertinoSliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    require(steps >= 0) { "steps should be >= 0" }

    CupertinoSliderImpl(
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        steps = steps,
        value = value,
        valueRange = valueRange,
        thumb = {
            CupertinoSliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = colors,
                enabled = enabled,
            )
        },
        track = { sliderPositions ->
            CupertinoSliderDefaults.Track(
                colors = colors,
                enabled = enabled,
                sliderPositions = sliderPositions,
            )
        },
    )
}

// region Colors

/**
 * Represents the colors used by a [CupertinoSlider] in different states.
 */
@Immutable
internal class CupertinoSliderColors(
    private val thumbColor: Color,
    private val activeTrackColor: Color,
    private val activeTickColor: Color,
    private val inactiveTrackColor: Color,
    private val inactiveTickColor: Color,
    private val disabledThumbColor: Color,
    private val disabledActiveTrackColor: Color,
    private val disabledActiveTickColor: Color,
    private val disabledInactiveTrackColor: Color,
    private val disabledInactiveTickColor: Color,
) {
    @Composable
    internal fun thumbColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(if (enabled) thumbColor else disabledThumbColor)

    @Composable
    internal fun trackColor(enabled: Boolean, active: Boolean): State<Color> =
        rememberUpdatedState(
            if (enabled) {
                if (active) activeTrackColor else inactiveTrackColor
            } else {
                if (active) disabledActiveTrackColor else disabledInactiveTrackColor
            }
        )

    @Composable
    internal fun tickColor(enabled: Boolean, active: Boolean): State<Color> =
        rememberUpdatedState(
            if (enabled) {
                if (active) activeTickColor else inactiveTickColor
            } else {
                if (active) disabledActiveTickColor else disabledInactiveTickColor
            }
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CupertinoSliderColors) return false
        if (thumbColor != other.thumbColor) return false
        if (activeTrackColor != other.activeTrackColor) return false
        if (activeTickColor != other.activeTickColor) return false
        if (inactiveTrackColor != other.inactiveTrackColor) return false
        if (inactiveTickColor != other.inactiveTickColor) return false
        if (disabledThumbColor != other.disabledThumbColor) return false
        if (disabledActiveTrackColor != other.disabledActiveTrackColor) return false
        if (disabledActiveTickColor != other.disabledActiveTickColor) return false
        if (disabledInactiveTrackColor != other.disabledInactiveTrackColor) return false
        if (disabledInactiveTickColor != other.disabledInactiveTickColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = thumbColor.hashCode()
        result = 31 * result + activeTrackColor.hashCode()
        result = 31 * result + activeTickColor.hashCode()
        result = 31 * result + inactiveTrackColor.hashCode()
        result = 31 * result + inactiveTickColor.hashCode()
        result = 31 * result + disabledThumbColor.hashCode()
        result = 31 * result + disabledActiveTrackColor.hashCode()
        result = 31 * result + disabledActiveTickColor.hashCode()
        result = 31 * result + disabledInactiveTrackColor.hashCode()
        result = 31 * result + disabledInactiveTickColor.hashCode()
        return result
    }
}

// endregion

// region Defaults

/**
 * Defaults for the Cupertino slider.
 */
internal object CupertinoSliderDefaults {

    /**
     * Creates a [CupertinoSliderColors] with default iOS-style colors.
     */
    fun colors(
        thumbColor: Color = Color.White,
        activeTrackColor: Color = CupertinoAccentBlue,
        activeTickColor: Color = CupertinoSeparator,
        inactiveTrackColor: Color = CupertinoSeparator,
        inactiveTickColor: Color = activeTickColor,
        disabledThumbColor: Color = thumbColor,
        disabledActiveTrackColor: Color = activeTrackColor.copy(alpha = 0.5f),
        disabledActiveTickColor: Color = activeTickColor,
        disabledInactiveTrackColor: Color = inactiveTrackColor.copy(alpha = 0.5f),
        disabledInactiveTickColor: Color = activeTickColor,
    ): CupertinoSliderColors = CupertinoSliderColors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        activeTickColor = activeTickColor,
        inactiveTrackColor = inactiveTrackColor,
        inactiveTickColor = inactiveTickColor,
        disabledThumbColor = disabledThumbColor,
        disabledActiveTrackColor = disabledActiveTrackColor,
        disabledActiveTickColor = disabledActiveTickColor,
        disabledInactiveTrackColor = disabledInactiveTrackColor,
        disabledInactiveTickColor = disabledInactiveTickColor,
    )

    /**
     * The default thumb for [CupertinoSlider].
     */
    @Composable
    fun Thumb(
        interactionSource: MutableInteractionSource,
        modifier: Modifier = Modifier,
        colors: CupertinoSliderColors = colors(),
        enabled: Boolean = true,
        thumbSize: DpSize = ThumbSize,
    ) {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> interactions.add(interaction)
                    is PressInteraction.Release -> interactions.remove(interaction.press)
                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                    is DragInteraction.Start -> interactions.add(interaction)
                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                }
            }
        }
        val shape = CircleShape
        Spacer(
            modifier
                .size(thumbSize)
                .hoverable(interactionSource = interactionSource)
                .shadow(if (enabled) ThumbElevation else 0.dp, shape, clip = false)
                .background(colors.thumbColor(enabled).value, shape)
        )
    }

    /**
     * The default track for [CupertinoSlider].
     */
    @Composable
    fun Track(
        sliderPositions: CupertinoSliderPositions,
        modifier: Modifier = Modifier,
        colors: CupertinoSliderColors = colors(),
        enabled: Boolean = true,
    ) {
        val inactiveTrackColor = colors.trackColor(enabled, active = false)
        val activeTrackColor = colors.trackColor(enabled, active = true)
        val inactiveTickColor = colors.tickColor(enabled, active = false)
        val activeTickColor = colors.tickColor(enabled, active = true)
        Canvas(
            modifier
                .fillMaxWidth()
                .height(TrackHeight)
        ) {
            val isRtl = layoutDirection == LayoutDirection.Rtl
            val sliderLeft = Offset(0f, center.y)
            val sliderRight = Offset(size.width, center.y)
            val sliderStart = if (isRtl) sliderRight else sliderLeft
            val sliderEnd = if (isRtl) sliderLeft else sliderRight
            val tickSize = TickSize.toPx()
            val trackStrokeWidth = TrackHeight.toPx()

            drawLine(
                color = inactiveTrackColor.value,
                start = sliderStart,
                end = sliderEnd,
                strokeWidth = trackStrokeWidth,
                cap = StrokeCap.Round,
            )

            val sliderValueEnd = Offset(
                sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.endInclusive,
                center.y,
            )
            val sliderValueStart = Offset(
                sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.start,
                center.y,
            )

            drawLine(
                color = activeTrackColor.value,
                start = sliderValueStart,
                end = sliderValueEnd,
                strokeWidth = trackStrokeWidth,
                cap = StrokeCap.Round,
            )

            sliderPositions.tickFractions.groupBy {
                it > sliderPositions.activeRange.endInclusive ||
                    it < sliderPositions.activeRange.start
            }.forEach { (outsideFraction, list) ->
                list.forEach {
                    drawLine(
                        color = (if (outsideFraction) inactiveTickColor else activeTickColor).value,
                        start = Offset(lerp(sliderStart, sliderEnd, it).x, center.y - 2.dp.toPx()),
                        end = Offset(lerp(sliderStart, sliderEnd, it).x, center.y + 2.dp.toPx()),
                        strokeWidth = tickSize,
                        cap = StrokeCap.Square,
                    )
                }
            }
        }
    }
}

// iOS-style colors
private val CupertinoAccentBlue = Color(0xFF007AFF)
private val CupertinoSeparator = Color(0x4D3C3C43)

// endregion

// region Tokens & dimensions

private val ThumbElevation = 8.dp
private val HandleHeight = 20.0.dp
private val HandleWidth = 20.0.dp
private val TrackHeight = 4.0.dp
private val TickSize = 3.0.dp

private val ThumbWidth = 26.dp
private val ThumbHeight = 26.dp
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)

private val SliderToTickAnimationForward = tween<Float>(durationMillis = 180, easing = LinearEasing)
private val SliderToTickAnimationBackward = tween<Float>(durationMillis = 80, easing = LinearEasing)

// endregion

// region SliderPositions

/**
 * Class that holds information about [CupertinoSlider]'s active track
 * and fractional positions where the discrete ticks should be drawn on the track.
 */
@Stable
internal class CupertinoSliderPositions(
    initialActiveRange: ClosedFloatingPointRange<Float> = 0f..1f,
    initialTickFractions: FloatArray = floatArrayOf(),
) {
    var activeRange: ClosedFloatingPointRange<Float> by mutableStateOf(initialActiveRange)
        internal set

    var tickFractions: FloatArray by mutableStateOf(initialTickFractions)
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CupertinoSliderPositions) return false
        if (activeRange != other.activeRange) return false
        if (!tickFractions.contentEquals(other.tickFractions)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = activeRange.hashCode()
        result = 31 * result + tickFractions.contentHashCode()
        return result
    }
}

// endregion

// region SliderImpl

@Composable
private fun CupertinoSliderImpl(
    modifier: Modifier,
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)?,
    steps: Int,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    thumb: @Composable (CupertinoSliderPositions) -> Unit,
    track: @Composable (CupertinoSliderPositions) -> Unit,
) {
    val onValueChangeState = rememberUpdatedState<(Float) -> Unit> {
        if (it != value) {
            onValueChange(it)
        }
    }

    val tickFractions = remember(steps) {
        stepsToTickFractions(steps)
    }

    val thumbWidth = remember { mutableStateOf(ThumbWidth.value) }
    val totalWidth = remember { mutableStateOf(0) }

    fun scaleToUserValue(minPx: Float, maxPx: Float, offset: Float) =
        scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

    fun scaleToOffset(minPx: Float, maxPx: Float, userValue: Float) =
        scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val rawOffset = remember {
        lazy {
            val maxPx = max(totalWidth.value - thumbWidth.value / 2, 0f)
            val minPx = min(thumbWidth.value / 2, maxPx)
            mutableStateOf(scaleToOffset(minPx, maxPx, value))
        }
    }

    val pressOffset = remember { mutableStateOf(0f) }
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val positionFraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)

    val sliderPositions = remember {
        CupertinoSliderPositions(0f..positionFraction, tickFractions)
    }
    sliderPositions.activeRange = 0f..positionFraction
    sliderPositions.tickFractions = tickFractions

    val draggableState = remember(valueRange) {
        CupertinoSliderDraggableState {
            val maxPx = max(totalWidth.value - thumbWidth.value / 2, 0f)
            val minPx = min(thumbWidth.value / 2, maxPx)
            rawOffset.value.value =
                (rawOffset.value.value + it + pressOffset.value).coerceIn(0f, totalWidth.value.toFloat())
            pressOffset.value = 0f
            val offsetInTrack = snapValueToTick(rawOffset.value.value, tickFractions, minPx, maxPx)
            val new = scaleToUserValue(minPx, maxPx, offsetInTrack)
            onValueChangeState.value.invoke(new)
        }
    }

    val gestureEndAction = rememberUpdatedState {
        if (!draggableState.isDragging) {
            onValueChangeFinished?.invoke()
        }
    }

    val press = Modifier.cupertinoSliderTapModifier(
        draggableState,
        interactionSource,
        totalWidth.value,
        isRtl,
        rawOffset,
        gestureEndAction,
        pressOffset,
        enabled,
    )

    val drag = Modifier.pointerInput(enabled, isRtl) {
        if (!enabled) return@pointerInput
        var dragInteraction: DragInteraction.Start? = null
        detectHorizontalDragGestures(
            onDragStart = {
                val interaction = DragInteraction.Start()
                dragInteraction = interaction
                interactionSource.tryEmit(interaction)
            },
            onDragEnd = {
                dragInteraction?.let {
                    interactionSource.tryEmit(DragInteraction.Stop(it))
                }
                dragInteraction = null
                gestureEndAction.value.invoke()
            },
            onDragCancel = {
                dragInteraction?.let {
                    interactionSource.tryEmit(DragInteraction.Cancel(it))
                }
                dragInteraction = null
                gestureEndAction.value.invoke()
            },
            onHorizontalDrag = { change, _ ->
                val halfThumb = thumbWidth.value / 2f
                val trackWidthPx = (totalWidth.value.toFloat() - thumbWidth.value).coerceAtLeast(1f)
                val positionOnTrack = change.position.x - halfThumb
                val fraction = (positionOnTrack / trackWidthPx).coerceIn(0f, 1f)
                val adjustedFraction = if (isRtl) 1f - fraction else fraction

                val snappedFraction = if (tickFractions.isNotEmpty()) {
                    tickFractions.minByOrNull { abs(it - adjustedFraction) } ?: adjustedFraction
                } else {
                    adjustedFraction
                }

                val newValue = lerp(valueRange.start, valueRange.endInclusive, snappedFraction)
                onValueChangeState.value.invoke(newValue.coerceIn(valueRange.start, valueRange.endInclusive))

                // Update rawOffset for tap animation compatibility
                val maxPx = max(totalWidth.value - thumbWidth.value / 2, 0f)
                val minPx = min(thumbWidth.value / 2, maxPx)
                rawOffset.value.value = scale(valueRange.start, valueRange.endInclusive, newValue, minPx, maxPx)

                change.consume()
            }
        )
    }

    Layout(
        {
            Box(modifier = Modifier.layoutId(CupertinoSliderComponents.THUMB)) {
                thumb(sliderPositions)
            }
            Box(modifier = Modifier.layoutId(CupertinoSliderComponents.TRACK)) {
                track(sliderPositions)
            }
        },
        modifier = modifier
            .requiredSizeIn(
                minWidth = HandleWidth,
                minHeight = HandleHeight,
            )
            .cupertinoSliderSemantics(
                value = value,
                enabled = enabled,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
                steps = steps,
            )
            .focusable(enabled, interactionSource)
            .then(press)
            .then(drag),
    ) { measurables, constraints ->
        val thumbPlaceable = measurables.first {
            it.layoutId == CupertinoSliderComponents.THUMB
        }.measure(constraints)
        val trackPlaceable = measurables.first {
            it.layoutId == CupertinoSliderComponents.TRACK
        }.measure(
            constraints.offset(
                horizontal = -thumbPlaceable.width,
            ).copy(minHeight = 0),
        )

        val sliderWidth = thumbPlaceable.width + trackPlaceable.width
        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)

        thumbWidth.value = thumbPlaceable.width.toFloat()
        totalWidth.value = sliderWidth

        val trackOffsetX = thumbPlaceable.width / 2
        val thumbOffsetX = ((trackPlaceable.width) * positionFraction).roundToInt()
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2

        layout(sliderWidth, sliderHeight) {
            trackPlaceable.placeRelative(trackOffsetX, trackOffsetY)
            thumbPlaceable.placeRelative(thumbOffsetX, thumbOffsetY)
        }
    }
}

// endregion

// region DraggableState

private class CupertinoSliderDraggableState(
    val onDelta: (Float) -> Unit,
) : DraggableState {
    var isDragging by mutableStateOf(false)
        private set

    private val dragScope: DragScope = object : DragScope {
        override fun dragBy(pixels: Float): Unit = onDelta(pixels)
    }

    private val scrollMutex = MutatorMutex()

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit,
    ): Unit = coroutineScope {
        isDragging = true
        scrollMutex.mutateWith(dragScope, dragPriority, block)
        isDragging = false
    }

    override fun dispatchRawDelta(delta: Float) {
        return onDelta(delta)
    }
}

// endregion

// region Components enum

private enum class CupertinoSliderComponents {
    THUMB,
    TRACK,
}

// endregion

// region Utility functions

private fun snapValueToTick(
    current: Float,
    tickFractions: FloatArray,
    minPx: Float,
    maxPx: Float,
): Float {
    return tickFractions
        .minByOrNull { abs(lerp(minPx, maxPx, it) - current) }
        ?.run { lerp(minPx, maxPx, this) }
        ?: current
}

private fun stepsToTickFractions(steps: Int): FloatArray {
    return if (steps == 0) floatArrayOf() else FloatArray(steps + 2) { it.toFloat() / (steps + 1) }
}

private fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

// endregion

// region Semantics modifier

private fun Modifier.cupertinoSliderSemantics(
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
): Modifier {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                var newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
                val originalVal = newValue
                val resolvedValue = if (steps > 0) {
                    var distance: Float = newValue
                    for (i in 0..steps + 1) {
                        val stepValue = lerp(
                            valueRange.start,
                            valueRange.endInclusive,
                            i.toFloat() / (steps + 1),
                        )
                        if (abs(stepValue - originalVal) <= distance) {
                            distance = abs(stepValue - originalVal)
                            newValue = stepValue
                        }
                    }
                    newValue
                } else {
                    newValue
                }
                if (resolvedValue == coerced) {
                    false
                } else {
                    onValueChange(resolvedValue)
                    onValueChangeFinished?.invoke()
                    true
                }
            },
        )
    }.progressSemantics(value, valueRange, steps)
}

// endregion

// region Tap modifier

private fun Modifier.cupertinoSliderTapModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    maxPx: Int,
    isRtl: Boolean,
    rawOffset: Lazy<State<Float>>,
    gestureEndAction: State<() -> Unit>,
    pressOffset: MutableState<Float>,
    enabled: Boolean,
) = composed(
    factory = {
        if (enabled) {
            val scope = rememberCoroutineScope()
            pointerInput(draggableState, interactionSource, maxPx, isRtl) {
                awaitEachGesture {
                    val event = awaitFirstDown()
                    val end = waitForUpOrCancellation() ?: return@awaitEachGesture
                    val slop = viewConfiguration.pointerSlop(event.type)
                    if (abs((event.position.x - end.position.x)) < slop) {
                        val offset = if (isRtl) {
                            maxPx - end.position.x
                        } else {
                            end.position.x
                        }
                        scope.launch {
                            cupertinoAnimateToTarget(
                                draggableState,
                                rawOffset.value.value,
                                offset,
                                0f,
                            )
                            gestureEndAction.value.invoke()
                        }
                    }
                }
            }
        } else {
            this
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "cupertinoSliderTapModifier"
        properties["draggableState"] = draggableState
        properties["interactionSource"] = interactionSource
        properties["maxPx"] = maxPx
        properties["isRtl"] = isRtl
        properties["rawOffset"] = rawOffset
        properties["gestureEndAction"] = gestureEndAction
        properties["pressOffset"] = pressOffset
        properties["enabled"] = enabled
    },
)

private suspend fun cupertinoAnimateToTarget(
    draggableState: DraggableState,
    current: Float,
    target: Float,
    velocity: Float,
) {
    draggableState.drag {
        var latestValue = current
        val anim = if (target > current) {
            SliderToTickAnimationForward
        } else {
            SliderToTickAnimationBackward
        }
        Animatable(initialValue = current).animateTo(target, anim, velocity) {
            dragBy(this.value - latestValue)
            latestValue = this.value
        }
    }
}

// endregion

// region Pointer slop

private val mouseSlop = 0.125.dp
private val defaultTouchSlop = 18.dp
private val mouseToTouchSlopRatio = mouseSlop / defaultTouchSlop

private fun ViewConfiguration.pointerSlop(pointerType: PointerType): Float {
    return when (pointerType) {
        PointerType.Mouse -> touchSlop * mouseToTouchSlopRatio
        else -> touchSlop
    }
}

// endregion
