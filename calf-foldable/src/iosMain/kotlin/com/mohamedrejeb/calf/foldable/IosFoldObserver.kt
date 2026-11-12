package com.mohamedrejeb.calf.foldable

import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import com.mohamedrejeb.calf.foldable.cinterop.CalfFoldableRegionKindDivision
import com.mohamedrejeb.calf.foldable.cinterop.CalfFoldableRegionKindOcclusion
import com.mohamedrejeb.calf.foldable.cinterop.calf_foldable_isHingeApiAvailable
import com.mohamedrejeb.calf.foldable.cinterop.calf_foldable_isRegionsApiAvailable
import com.mohamedrejeb.calf.foldable.cinterop.calf_foldable_makeHingeInteraction
import com.mohamedrejeb.calf.foldable.cinterop.calf_foldable_reservedRegions
import com.mohamedrejeb.calf.foldable.cinterop.calf_foldable_runtimeReport
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSBundle
import platform.Foundation.NSNumber
import platform.UIKit.UIInteractionProtocol
import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceSizeClass
import platform.UIKit.UIUserInterfaceSizeClassCompact
import platform.UIKit.UIUserInterfaceSizeClassRegular
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.addInteraction
import platform.UIKit.removeInteraction

/**
 * Invisible view covering the host view. Reserved regions lag the hinge (by about a second when
 * folding) and nothing announces the change, but UIKit lays a view out again when a region it
 * read *during layout* changes. So the regions are read here and nowhere else.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosFoldProbeView(
    private val onLayout: () -> Unit,
) : UIView(frame = CGRectZero.readValue()) {

    override fun layoutSubviews() {
        super.layoutSubviews()
        onLayout()
    }

    // Size classes can change without the view's size changing (iPad Split View keeps some).
    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        setNeedsLayout()
    }
}

/**
 * Feeds a [FoldState] from UIKit for as long as it is attached to [hostView]: the posture and the
 * angle from a `UIHingeInteraction`, the regions and size classes from a probe view's layout pass.
 * Main thread only, like the UIKit APIs it wraps.
 *
 * @param density pixels per point, read on every update so a moved window uses the new value.
 */
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class, ExperimentalForeignApi::class)
internal class IosFoldObserver(
    private val hostView: UIView,
    private val density: () -> Float,
) {
    // The latest angle is replayed to a new collector; a slow collector only ever misses old angles.
    private val hingeAngle = MutableSharedFlow<Float>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val state: FoldState = FoldState(
        initialSnapshot = FoldSnapshot(iosCapabilities = currentCapabilities(hasHinge = null)),
        iosHingeAngle = hingeAngle.asSharedFlow(),
        iosDebugDumpProvider = ::debugDump,
    )

    private var probeView: IosFoldProbeView? = null
    private var hingeInteraction: UIInteractionProtocol? = null
    private var posture: FoldPosture = FoldPosture.NoFold
    private var hasHinge: Boolean? = null
    private var reading: IosFoldReading = IosFoldReading.Empty

    fun attach() {
        if (probeView != null) return

        val probe = IosFoldProbeView(onLayout = ::onProbeLayout).apply {
            userInteractionEnabled = false
            setFrame(hostView.bounds)
            autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        }
        hostView.insertSubview(probe, atIndex = 0)
        probeView = probe

        hingeInteraction = calf_foldable_makeHingeInteraction(::onHingeUpdate)
            ?.also(hostView::addInteraction)
    }

    /**
     * Must be called when the state is no longer used. The interaction holds the update handler,
     * which holds this observer: removing it is what lets all three go.
     */
    fun detach() {
        hingeInteraction?.let(hostView::removeInteraction)
        hingeInteraction = null
        probeView?.removeFromSuperview()
        probeView = null
    }

    /** The only place regions are read: UIKit only re-runs layout for regions read during layout. */
    private fun onProbeLayout() {
        val probe = probeView ?: return
        val pixelsPerPoint = density()
        reading = IosFoldReading(
            divisionRegions = probe.reservedRegions(CalfFoldableRegionKindDivision, pixelsPerPoint),
            occlusionRegions = probe.reservedRegions(CalfFoldableRegionKindOcclusion, pixelsPerPoint),
            horizontalSizeClass = probe.traitCollection.horizontalSizeClass.toIosSizeClass(),
            verticalSizeClass = probe.traitCollection.verticalSizeClass.toIosSizeClass(),
        )
        publishSnapshot()
    }

    private fun onHingeUpdate(hasHingeNow: Boolean, rawStatus: Long, angleRadians: Double) {
        // A nil hinge means "left a hierarchy that provides hinge updates", not "no hinge".
        if (hasHingeNow) {
            hasHinge = true
            hingeAngle.tryEmit(hingeDegreesOf(angleRadians))
        }

        val newPosture = if (hasHingeNow) foldPostureOfIosHingeStatus(rawStatus) else FoldPosture.NoFold
        if (newPosture == posture && state.snapshot.iosCapabilities.hasHinge == hasHinge) return

        // The posture is published over the regions of the last layout pass, so isSeparating
        // follows the hinge at once. The regions themselves catch up in layout; asking for a pass
        // costs nothing and does not rely on UIKit noticing by itself.
        posture = newPosture
        publishSnapshot()
        probeView?.setNeedsLayout()
    }

    private fun publishSnapshot() {
        val snapshot = iosFoldSnapshotOf(posture, reading, currentCapabilities(hasHinge))
        // Layout passes and angle ticks are frequent; only a real change may invalidate Compose readers.
        if (snapshot != state.snapshot) state.snapshot = snapshot
    }

    private fun debugDump(snapshot: FoldSnapshot): String =
        buildString {
            appendLine("Calf FoldState (iOS)")
            appendLine("app built with SDK: ${NSBundle.mainBundle.objectForInfoDictionaryKey("DTSDKName") ?: "unknown"}")
            appendLine("(reserved regions stay empty unless that is iphoneos27.1 / iphonesimulator27.1 or later)")
            appendLine(calf_foldable_runtimeReport())
            appendLine("attached: ${probeView != null}, hinge interaction: ${hingeInteraction != null}")
            append(snapshot)
        }
}

@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class, ExperimentalForeignApi::class)
private fun currentCapabilities(hasHinge: Boolean?): IosFoldCapabilities =
    IosFoldCapabilities(
        isHingeApiAvailable = calf_foldable_isHingeApiAvailable(),
        isRegionsApiAvailable = calf_foldable_isRegionsApiAvailable(),
        hasHinge = hasHinge,
    )

/** Inactive regions are included: the fold keeps its bounds while the device is flat. */
@OptIn(ExperimentalCalfFoldableApi::class, ExperimentalForeignApi::class)
private fun UIView.reservedRegions(kind: Int, density: Float): List<FoldRegion> =
    calf_foldable_reservedRegions(this, kind, includeInactive = true)
        .mapNotNull { entry ->
            val values = (entry as? List<*>)?.map { (it as? NSNumber)?.doubleValue ?: Double.NaN }
            values?.let { foldRegionOfIosValues(it, density) }
        }

@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
private fun UIUserInterfaceSizeClass.toIosSizeClass(): IosSizeClass =
    when (this) {
        UIUserInterfaceSizeClassCompact -> IosSizeClass.Compact
        UIUserInterfaceSizeClassRegular -> IosSizeClass.Regular
        else -> IosSizeClass.Unspecified
    }
