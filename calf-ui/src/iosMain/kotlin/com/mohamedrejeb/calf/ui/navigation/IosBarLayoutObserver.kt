package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectZero
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UILayoutPriorityDefaultLow
import platform.UIKit.UIUserInterfaceSizeClassCompact
import platform.UIKit.UIView

/** The host view edge a native bar is pinned to. */
internal enum class IosBarEdge {
    Top,
    Bottom,
}

/**
 * Invisible view stretched over everything a native bar claims: from the host
 * view's edge, across the safe-area inset, to the far side of the bar. Its size
 * therefore changes whenever the bar's size or that inset does (rotation, split
 * view, large title, in-call status bar), and UIKit then lays it out again.
 * This replaces polling the bar's frame every frame.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosBarLayoutProbeView(
    private val onLayoutChanged: () -> Unit,
) : UIView(frame = CGRectZero.readValue()) {

    override fun layoutSubviews() {
        super.layoutSubviews()
        onLayoutChanged()
    }

    // Insets can change before Auto Layout has moved the bar (and already do when this view is
    // attached), so only ask for a layout pass here and measure once the frames are final.
    override fun safeAreaInsetsDidChange() {
        super.safeAreaInsetsDidChange()
        setNeedsLayout()
    }
}

/**
 * Adds [barView] to the hosting view controller's view, pinned to [edge], and
 * keeps the returned layout in sync with UIKit for as long as the caller stays
 * in the composition. The bar's content padding is mirrored into
 * [contentPaddingState] for [AdaptiveScaffold].
 *
 * @param barCoversSafeArea the bar extends under the safe-area inset of its
 *   edge instead of sitting next to it.
 * @param minimumEdgeOffset smallest distance the bar keeps from the host view's
 *   edge when the safe-area inset is smaller than that.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun rememberIosBarLayout(
    barView: UIView,
    edge: IosBarEdge,
    kind: IosBarKind,
    barCoversSafeArea: Boolean,
    contentPaddingState: MutableState<PaddingValues>,
    minimumEdgeOffset: Dp = 0.dp,
): State<IosBarLayout> {
    val hostView = LocalUIViewController.current.view
    val currentKind by rememberUpdatedState(kind)

    val layoutState = remember(barView, hostView, edge, barCoversSafeArea, minimumEdgeOffset) {
        val estimatedLayout = IosBarLayout.estimated(
            cachedBarHeight = IosBarHeightCache.heightFor(hostView.barHeightKey(kind)),
            edgeOffset = iosBarEdgeOffset(
                safeAreaInset = hostView.safeAreaInset(edge),
                barCoversSafeArea = barCoversSafeArea,
                minimumOffset = minimumEdgeOffset,
            ),
        )
        // Published while composing, not from an effect: the scaffold composes its content
        // after its bars, so the content's very first frame already has this padding.
        contentPaddingState.value = edge.paddingValues(estimatedLayout.contentPadding)
        mutableStateOf(estimatedLayout)
    }

    DisposableEffect(layoutState, contentPaddingState) {
        // The padding state belongs to the scaffold and outlives this bar. A bar that this one
        // replaces is disposed after this one composed, so it must not wipe what this one wrote:
        // publish again now that the old bar is gone, and only ever clear a value of our own.
        var publishedPadding = edge.paddingValues(layoutState.value.contentPadding)
        contentPaddingState.value = publishedPadding

        val probeView = IosBarLayoutProbeView(
            onLayoutChanged = {
                val measurement = barView.measureIn(hostView, edge)
                IosBarHeightCache.update(hostView.barHeightKey(currentKind), measurement.barHeight)

                val measuredLayout = layoutState.value.measured(measurement)
                layoutState.value = measuredLayout
                publishedPadding = edge.paddingValues(measuredLayout.contentPadding)
                contentPaddingState.value = publishedPadding
            },
        ).apply {
            translatesAutoresizingMaskIntoConstraints = false
            userInteractionEnabled = false
        }

        hostView.addSubview(barView)
        hostView.insertSubview(probeView, belowSubview = barView)
        NSLayoutConstraint.activateConstraints(
            barConstraints(barView, probeView, hostView, edge, barCoversSafeArea, minimumEdgeOffset)
        )

        onDispose {
            probeView.removeFromSuperview()
            barView.removeFromSuperview()
            if (contentPaddingState.value == publishedPadding) {
                contentPaddingState.value = PaddingValues()
            }
        }
    }

    return layoutState
}

/**
 * Empty Compose node that reserves the native bar's room in the scaffold's bar
 * slot and follows the bar's frame. [layout] is only read while measuring and
 * drawing, so a new measurement never recomposes the bar.
 */
@Composable
internal fun IosBarPlaceholder(layout: () -> IosBarLayout) {
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .onPlaced { positionInRoot = it.positionInRoot() }
            .graphicsLayer {
                val barLayout = layout()
                translationX = barLayout.x.toPx() - positionInRoot.x
                translationY = barLayout.y.toPx() - positionInRoot.y
            }
            .layout { measurable, constraints ->
                val barLayout = layout()
                val width = constraints.constrainWidth(barLayout.width.roundToPx())
                val height = constraints.constrainHeight(barLayout.contentPadding.roundToPx())
                val placeable = measurable.measure(Constraints.fixed(width, height))
                layout(width, height) { placeable.place(0, 0) }
            }
    )
}

private fun barConstraints(
    barView: UIView,
    probeView: UIView,
    hostView: UIView,
    edge: IosBarEdge,
    barCoversSafeArea: Boolean,
    minimumEdgeOffset: Dp,
): List<NSLayoutConstraint> {
    val probeTopAnchor = when (edge) {
        IosBarEdge.Top -> hostView.topAnchor
        IosBarEdge.Bottom -> barView.topAnchor
    }
    val probeBottomAnchor = when (edge) {
        IosBarEdge.Top -> barView.bottomAnchor
        IosBarEdge.Bottom -> hostView.bottomAnchor
    }

    return barEdgeConstraints(barView, hostView, edge, barCoversSafeArea, minimumEdgeOffset) + listOf(
        barView.leadingAnchor.constraintEqualToAnchor(hostView.leadingAnchor),
        barView.trailingAnchor.constraintEqualToAnchor(hostView.trailingAnchor),
        probeView.leadingAnchor.constraintEqualToAnchor(hostView.leadingAnchor),
        probeView.trailingAnchor.constraintEqualToAnchor(hostView.trailingAnchor),
        probeView.topAnchor.constraintEqualToAnchor(probeTopAnchor),
        probeView.bottomAnchor.constraintEqualToAnchor(probeBottomAnchor),
    )
}

/**
 * Auto Layout version of [iosBarEdgeOffset]: the bar is pulled towards the host view's edge and
 * stopped by whichever is further in, the safe area or [minimumEdgeOffset]. UIKit re-solves it
 * on every safe-area change, so nothing has to be updated by hand on rotation.
 */
private fun barEdgeConstraints(
    barView: UIView,
    hostView: UIView,
    edge: IosBarEdge,
    barCoversSafeArea: Boolean,
    minimumEdgeOffset: Dp,
): List<NSLayoutConstraint> {
    val safeArea = hostView.safeAreaLayoutGuide
    val minimumOffset = minimumEdgeOffset.value.toDouble()

    return when (edge) {
        IosBarEdge.Top -> {
            val attachToEdge = barView.topAnchor.constraintEqualToAnchor(hostView.topAnchor)
            if (barCoversSafeArea)
                listOf(attachToEdge)
            else
                listOf(
                    attachToEdge.apply { priority = UILayoutPriorityDefaultLow },
                    barView.topAnchor.constraintGreaterThanOrEqualToAnchor(safeArea.topAnchor),
                    barView.topAnchor.constraintGreaterThanOrEqualToAnchor(hostView.topAnchor, minimumOffset),
                )
        }

        IosBarEdge.Bottom -> {
            val attachToEdge = barView.bottomAnchor.constraintEqualToAnchor(hostView.bottomAnchor)
            if (barCoversSafeArea)
                listOf(attachToEdge)
            else
                listOf(
                    attachToEdge.apply { priority = UILayoutPriorityDefaultLow },
                    barView.bottomAnchor.constraintLessThanOrEqualToAnchor(safeArea.bottomAnchor),
                    barView.bottomAnchor.constraintLessThanOrEqualToAnchor(hostView.bottomAnchor, -minimumOffset),
                )
        }
    }
}

private fun IosBarEdge.paddingValues(contentPadding: Dp): PaddingValues =
    when (this) {
        IosBarEdge.Top -> PaddingValues(top = contentPadding)
        IosBarEdge.Bottom -> PaddingValues(bottom = contentPadding)
    }

private fun UIView.barHeightKey(kind: IosBarKind): IosBarHeightKey =
    IosBarHeightKey(
        kind = kind,
        isCompactHeight = traitCollection.verticalSizeClass == UIUserInterfaceSizeClassCompact,
    )

@OptIn(ExperimentalForeignApi::class)
private fun UIView.safeAreaInset(edge: IosBarEdge): Dp =
    safeAreaInsets.useContents {
        when (edge) {
            IosBarEdge.Top -> top.dp
            IosBarEdge.Bottom -> bottom.dp
        }
    }

/** [this] must be a direct subview of [hostView], so its frame is already in host coordinates. */
@OptIn(ExperimentalForeignApi::class)
private fun UIView.measureIn(hostView: UIView, edge: IosBarEdge): IosBarMeasurement {
    val hostHeight = hostView.bounds.useContents { size.height }
    return frame.useContents {
        val edgeOffset = when (edge) {
            IosBarEdge.Top -> origin.y
            IosBarEdge.Bottom -> hostHeight - (origin.y + size.height)
        }
        IosBarMeasurement(
            x = origin.x.dp,
            y = origin.y.dp,
            width = size.width.dp,
            barHeight = size.height.dp,
            edgeOffset = edgeOffset.dp,
        )
    }
}
