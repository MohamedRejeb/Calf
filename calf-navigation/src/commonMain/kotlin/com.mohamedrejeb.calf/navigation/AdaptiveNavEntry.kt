package com.mohamedrejeb.calf.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.metadata
import com.mohamedrejeb.calf.navigation.internal.AdaptiveNavEntryMetadata
import com.mohamedrejeb.calf.navigation.internal.AdaptiveNavEntryMetadataKey
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem

/**
 * Builds a [NavEntry] whose bar is painted by the navigator on every platform.
 * The screen's [content] renders only the body and applies the received
 * [PaddingValues]: the Material `Scaffold` padding, or the safe-area insets on
 * iOS, where the translucent native bars sit over edge-to-edge content. Pass
 * them as `contentPadding` on lazy containers so content scrolls under the
 * bars but starts below them.
 *
 * The signature mirrors `AdaptiveTopBar`: composable slots for Material and
 * structured `ios*` data for iOS. Structured data is required on iOS because
 * it is applied to `UINavigationItem` *before* the push animation starts,
 * which is what makes the native title cross-fade animate with the right
 * content.
 *
 * Parameters with composable defaults ([expandedHeight], [windowInsets],
 * [colors]) are nullable because this builder runs inside the non-composable
 * `entryProvider` lambda; `null` resolves to the platform default inside the
 * entry's composition.
 *
 * @param key the typed route. Make it `@Serializable` if the back stack is
 *   persisted through a `SavedStateConfiguration`.
 * @param title Material `TopAppBar` title slot.
 * @param navigationIcon Material `TopAppBar` navigation-icon slot.
 * @param actions Material `TopAppBar` actions slot.
 * @param expandedHeight Material `TopAppBar` expanded height (`null` = default).
 * @param windowInsets Material `TopAppBar` window insets (`null` = default).
 * @param colors Material `TopAppBar` colors (`null` = default).
 * @param scrollBehavior Material `TopAppBar` scroll behavior. When `null` and
 *   [largeTitle] is true, an exit-until-collapsed behavior is created so the
 *   large title collapses on scroll.
 * @param largeTitle renders a collapsing `LargeTopAppBar` on Material and a
 *   large title that shrinks on scroll on iOS. This is the only large-title
 *   switch; `iosConfiguration.prefersLargeTitles` is ignored by the navigator.
 * @param iosTitle the title shown in the iOS `UINavigationBar`.
 * @param iosBackTitle label of the back button shown on the *next* pushed screen.
 * @param iosLeadingItems left-aligned bar items on iOS.
 * @param iosTrailingItems right-aligned bar items on iOS.
 * @param iosConfiguration appearance of the iOS bar (translucency, title color).
 * @param barShown when false, the navigator hides its top bar for this entry.
 * @param tabBarShown when false, a hosting [AdaptiveTabDisplay] hides its tab
 *   bar while this entry is on top: `hidesBottomBarWhenPushed` on iOS, an
 *   animated hide of the Material navigation bar elsewhere. Ignored outside
 *   tabs.
 * @param iosGestureEnabled when false, disables iOS swipe-back for this entry.
 * @param paneRole the entry's place in a list-detail layout: [AdaptivePaneRole.List]
 *   for the list, [AdaptivePaneRole.Detail] for the first screen of the detail
 *   side. Only read by a display that was given `listDetail` settings.
 * @param content the screen body, receiving the bar-inset [PaddingValues].
 */
@ExperimentalCalfNavigationApi
@OptIn(ExperimentalMaterial3Api::class)
fun <T : NavKey> adaptiveNavEntry(
    key: T,
    // ── Material composables (Android / Desktop / Web) ──
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp? = null,
    windowInsets: WindowInsets? = null,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    largeTitle: Boolean = false,
    // ── iOS structured data (ignored on Material) ──
    iosTitle: String = "",
    iosBackTitle: String? = null,
    iosLeadingItems: List<UIKitUIBarButtonItem> = emptyList(),
    iosTrailingItems: List<UIKitUIBarButtonItem> = emptyList(),
    iosConfiguration: UIKitNavigationBarConfiguration = UIKitNavigationBarConfiguration(),
    // ── Cross-platform behavior ──
    barShown: Boolean = true,
    tabBarShown: Boolean = true,
    iosGestureEnabled: Boolean = true,
    paneRole: AdaptivePaneRole = AdaptivePaneRole.None,
    content: @Composable (PaddingValues) -> Unit,
): NavEntry<NavKey> {
    val entryMetadata = AdaptiveNavEntryMetadata(
        iosTitle = iosTitle,
        iosBackTitle = iosBackTitle,
        iosLeadingItems = iosLeadingItems,
        iosTrailingItems = iosTrailingItems,
        iosConfiguration = iosConfiguration,
        largeTitle = largeTitle,
        barShown = barShown,
        tabBarShown = tabBarShown,
        iosGestureEnabled = iosGestureEnabled,
        paneRole = paneRole,
    )

    return NavEntry(
        key = key,
        metadata = metadata { put(AdaptiveNavEntryMetadataKey, entryMetadata) },
    ) {
        AdaptiveBarHostScope(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets ?: TopAppBarDefaults.windowInsets,
            colors = colors ?: TopAppBarDefaults.topAppBarColors(),
            scrollBehavior = scrollBehavior,
            largeTitle = largeTitle,
            barShown = barShown,
            content = content,
        )
    }
}
