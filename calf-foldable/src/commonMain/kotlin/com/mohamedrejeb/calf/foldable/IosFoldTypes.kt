package com.mohamedrejeb.calf.foldable

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.calf.core.CalfIosOnlyApi

/**
 * A UIKit size class (`UIUserInterfaceSizeClass`). iOS lays its own containers out from these: the
 * Duo's cover display is compact wide, its inner display regular. Unlike a width breakpoint they
 * also follow iPad Split View and Slide Over.
 */
@ExperimentalCalfFoldableApi
@CalfIosOnlyApi
enum class IosSizeClass {
    /** Not running on iOS, or UIKit has not decided yet. */
    Unspecified,
    Compact,
    Regular,
}

/**
 * What the running iOS offers, for diagnosing reports from devices you do not have.
 *
 * @property isHingeApiAvailable `UIHingeInteraction` exists (iOS 27.1 and later).
 * @property isRegionsApiAvailable `UIView.reservedRegionsOfKind:options:` exists (iOS 27.1 and
 *   later). Regions are still empty unless the **app** is built with the iOS 27.1 SDK.
 * @property hasHinge `true` once iOS has delivered a hinge for this window, `null` until then. It
 *   never becomes `false`: iOS does not say "this device has no hinge", it just stays silent.
 */
@ExperimentalCalfFoldableApi
@CalfIosOnlyApi
@Immutable
data class IosFoldCapabilities(
    val isHingeApiAvailable: Boolean = false,
    val isRegionsApiAvailable: Boolean = false,
    val hasHinge: Boolean? = null,
) {
    companion object {
        /** The value on every platform other than iOS, and on iOS before 27.1. */
        val Unavailable: IosFoldCapabilities = IosFoldCapabilities()
    }
}
