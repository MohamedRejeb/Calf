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
import com.mohamedrejeb.calf.navigation.internal.IosNavBarData
import com.mohamedrejeb.calf.navigation.internal.IosNavBarDataKey
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem

/**
 * Build a [NavEntry] with adaptive bar options. The navigator paints the top bar
 * from these options on every platform; the screen's [content] should render only
 * the body, applying the received [PaddingValues] — the Material `Scaffold`
 * padding, or the safe-drawing insets on iOS, where the translucent native bar
 * sits over edge-to-edge content. Pass it as `contentPadding` on lazy
 * containers so content scrolls under the bar but starts below it.
 *
 * The signature deliberately mirrors `AdaptiveTopBar`'s expect function:
 * composable slots for Material; structured `iosTitle` / `iosLeadingItems` /
 * `iosTrailingItems` / `iosConfiguration` for iOS. iOS values are stuffed into
 * [NavEntry.metadata] under [IosNavBarDataKey] so the iOS adapter can apply them
 * to `UINavigationItem` *before* the child VC is pushed — that's what makes the
 * native cross-fade animation work correctly.
 *
 * **Material defaults note:** parameters with composable defaults
 * ([expandedHeight], [windowInsets], [colors]) are nullable here because this
 * builder is invoked from a non-composable `entryProvider` lambda passed to
 * [AdaptiveNavDisplay]. Pass `null` to use the platform default; the value is
 * resolved inside the entry's @Composable content scope.
 *
 * @param key the typed route ([NavKey]). Must be `@Serializable`.
 * @param title Material `TopAppBar` title slot.
 * @param navigationIcon Material `TopAppBar` navigation-icon slot.
 * @param actions Material `TopAppBar` actions slot.
 * @param expandedHeight Material `TopAppBar` expanded height (`null` → default).
 * @param windowInsets Material `TopAppBar` window insets (`null` → default).
 * @param colors Material `TopAppBar` colors (`null` → default).
 * @param scrollBehavior Material `TopAppBar` scroll behavior. When `null` and
 *   [largeTitle] is true, an exit-until-collapsed behavior is created so the
 *   large title collapses on scroll.
 * @param largeTitle on Material renders a collapsing `LargeTopAppBar`; on iOS sets
 *   `navigationItem.largeTitleDisplayMode = .always` so the title shrinks on scroll.
 * @param iosTitle the title shown in the iOS `UINavigationBar`.
 * @param iosBackTitle title used by the back button on the *next* pushed screen
 *   (sets `navigationItem.backBarButtonItem.title`).
 * @param iosLeadingItems left-aligned bar items on iOS.
 * @param iosTrailingItems right-aligned bar items on iOS.
 * @param iosConfiguration appearance config for the iOS nav bar (translucency, tint, …).
 * @param barShown when false, the navigator hides its bar for this entry.
 * @param iosPresentation reserved for future modal-presentation styles; currently
 *   only [NavPresentation.Card] is implemented and other values fall back to it.
 * @param iosGestureEnabled when false, disables iOS swipe-back from the edge.
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
    iosPresentation: NavPresentation = NavPresentation.Card,
    iosGestureEnabled: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
): NavEntry<NavKey> {
    val iosData = IosNavBarData(
        title = iosTitle,
        backTitle = iosBackTitle,
        leadingItems = iosLeadingItems,
        trailingItems = iosTrailingItems,
        configuration = iosConfiguration,
        largeTitle = largeTitle,
        barShown = barShown,
        gestureEnabled = iosGestureEnabled,
        presentation = iosPresentation,
    )

    return NavEntry(
        key = key,
        metadata = mapOf(IosNavBarDataKey to iosData),
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
