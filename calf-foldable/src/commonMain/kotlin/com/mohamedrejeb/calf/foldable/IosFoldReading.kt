package com.mohamedrejeb.calf.foldable

/**
 * What one UIKit layout pass of the probe view read. Kept apart from the posture because the two
 * arrive separately: the hinge reports a new posture up to a second before UIKit lays the regions
 * out again, and regions may only be read during layout.
 */
internal data class IosFoldReading(
    val divisionRegions: List<FoldRegion> = emptyList(),
    val occlusionRegions: List<FoldRegion> = emptyList(),
    val horizontalSizeClass: IosSizeClass = IosSizeClass.Unspecified,
    val verticalSizeClass: IosSizeClass = IosSizeClass.Unspecified,
) {
    companion object {
        /** Before the first layout pass. */
        val Empty: IosFoldReading = IosFoldReading()
    }
}

/** The latest [posture] over the latest [reading]. A device has one fold: the first division region. */
internal fun iosFoldSnapshotOf(
    posture: FoldPosture,
    reading: IosFoldReading,
    capabilities: IosFoldCapabilities,
): FoldSnapshot {
    val divisionRegion = reading.divisionRegions.firstOrNull()
    return FoldSnapshot(
        posture = posture,
        fold = foldInfoOfIosRegion(divisionRegion, posture),
        iosFoldRegion = divisionRegion,
        iosOcclusionRegions = reading.occlusionRegions,
        iosHorizontalSizeClass = reading.horizontalSizeClass,
        iosVerticalSizeClass = reading.verticalSizeClass,
        iosCapabilities = capabilities,
    )
}
