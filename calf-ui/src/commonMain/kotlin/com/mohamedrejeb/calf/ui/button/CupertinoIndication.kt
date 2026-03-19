package com.mohamedrejeb.calf.ui.button

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Minimum duration (ms) the pressed alpha is held so that quick taps remain visible.
 */
private const val MinPressedDurationMillis = 100L

internal val DefaultCupertinoIndication = CupertinoIndication()

/**
 * Creates a Cupertino-style indication that applies alpha to the entire content on press,
 * matching native iOS button behavior (opacity feedback).
 *
 * @param pressedAlpha The alpha value when pressed (default 0.33f, matching iOS).
 */
internal fun CupertinoIndication(
    pressedAlpha: Float = CupertinoButtonDefaults.PressedAlpha,
): IndicationNodeFactory = CupertinoIndicationNodeFactory(pressedAlpha)

private class CupertinoIndicationNodeFactory(
    private val pressedAlpha: Float,
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return CupertinoIndicationNode(interactionSource, pressedAlpha)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CupertinoIndicationNodeFactory) return false
        return pressedAlpha == other.pressedAlpha
    }

    override fun hashCode(): Int = pressedAlpha.hashCode()
}

private class CupertinoIndicationNode(
    private val interactionSource: InteractionSource,
    private val pressedAlpha: Float,
) : Modifier.Node(), DrawModifierNode {

    private var alpha = 1f
    private val animatable = Animatable(1f)
    private val layerPaint = Paint()
    private var lastPressMark = TimeSource.Monotonic.markNow()

    override fun onAttach() {
        coroutineScope.launch {
            val activePress = mutableListOf<PressInteraction.Press>()
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        activePress.add(interaction)
                        lastPressMark = TimeSource.Monotonic.markNow()
                        launch {
                            animatable.snapTo(pressedAlpha)
                            alpha = pressedAlpha
                            invalidateDraw()
                        }
                    }
                    is PressInteraction.Release -> {
                        activePress.remove(interaction.press)
                        if (activePress.isEmpty()) {
                            launch {
                                // Ensure pressed state is visible for at least MinPressedDurationMillis
                                val elapsed = lastPressMark.elapsedNow().inWholeMilliseconds
                                val remaining = MinPressedDurationMillis - elapsed
                                if (remaining > 0) {
                                    delay(remaining)
                                }
                                animatable.animateTo(1f, tween(250)) {
                                    alpha = value
                                    invalidateDraw()
                                }
                            }
                        }
                    }
                    is PressInteraction.Cancel -> {
                        activePress.remove(interaction.press)
                        if (activePress.isEmpty()) {
                            launch {
                                val elapsed = lastPressMark.elapsedNow().inWholeMilliseconds
                                val remaining = MinPressedDurationMillis - elapsed
                                if (remaining > 0) {
                                    delay(remaining)
                                }
                                animatable.animateTo(1f, tween(250)) {
                                    alpha = value
                                    invalidateDraw()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        if (alpha >= 1f) {
            drawContent()
        } else {
            drawIntoCanvas { canvas ->
                layerPaint.alpha = alpha
                canvas.saveLayer(
                    bounds = Rect(0f, 0f, size.width, size.height),
                    paint = layerPaint,
                )
                drawContent()
                canvas.restore()
            }
        }
    }
}
