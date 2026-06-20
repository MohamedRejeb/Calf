package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi
import com.mohamedrejeb.calf.navigation.NavPresentation
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem

/**
 * Snapshot of every iOS-only navigation-bar property captured at navigator
 * definition time. Stored under [IosNavBarDataKey] in [androidx.navigation3.runtime.NavEntry.metadata]
 * so the iOS adapter can populate `UINavigationItem` *before* a child VC is
 * pushed — that's what makes the system cross-fade animation use the right
 * title and items during the transition.
 *
 * Material platforms ignore this entirely; they read the equivalent
 * composables (title, navigationIcon, actions) from the entry's content
 * lambda via `AdaptiveBarHostScope`.
 */
@OptIn(ExperimentalCalfNavigationApi::class)
@Immutable
internal data class IosNavBarData(
    val title: String,
    val backTitle: String?,
    val leadingItems: List<UIKitUIBarButtonItem>,
    val trailingItems: List<UIKitUIBarButtonItem>,
    val configuration: UIKitNavigationBarConfiguration,
    val largeTitle: Boolean,
    val barShown: Boolean,
    val gestureEnabled: Boolean,
    val presentation: NavPresentation,
) {
    companion object {
        /** Default for entries that weren't created via `adaptiveNavEntry`. */
        val Default = IosNavBarData(
            title = "",
            backTitle = null,
            leadingItems = emptyList(),
            trailingItems = emptyList(),
            configuration = UIKitNavigationBarConfiguration(),
            largeTitle = false,
            barShown = true,
            gestureEnabled = true,
            presentation = NavPresentation.Card,
        )
    }
}

/** Metadata key under which [IosNavBarData] is stored on a `NavEntry`. */
internal const val IosNavBarDataKey: String = "calf.nav.ios-bar-data"
