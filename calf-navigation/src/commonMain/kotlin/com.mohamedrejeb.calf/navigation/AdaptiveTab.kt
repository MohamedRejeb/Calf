package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

/**
 * How one tab of an [AdaptiveTabDisplay] is presented in the tab bar. Create
 * it with [adaptiveTab].
 */
@ExperimentalCalfNavigationApi
@Immutable
class AdaptiveTab<T : NavKey> internal constructor(
    /** The tab's root key; also the first entry of its back stack. */
    val key: T,
    /** Material navigation item icon. */
    val icon: @Composable () -> Unit,
    /** Material navigation item label, or `null` for icon-only. */
    val label: (@Composable () -> Unit)?,
    /** Material: show the label even when the tab is not selected. */
    val alwaysShowLabel: Boolean,
    /** Badge text on both platforms, or `null` for none. */
    val badge: String?,
    /** Title of the native iOS tab bar item. */
    val iosTitle: String,
    /** Image of the native iOS tab bar item. */
    val iosImage: UIKitImage?,
    /** Image of the native iOS tab bar item while selected; defaults to [iosImage]. */
    val iosSelectedImage: UIKitImage?,
) {
    /** The value-typed part of this tab, so hosts can detect changes without comparing lambdas. */
    internal val iosItem: IosTabItem
        get() = IosTabItem(iosTitle, iosImage, iosSelectedImage, badge)
}

/** Everything the native iOS tab bar item is built from; equality drives item refreshes. */
internal data class IosTabItem(
    val title: String,
    val image: UIKitImage?,
    val selectedImage: UIKitImage?,
    val badge: String?,
)

/**
 * Describes a tab for [AdaptiveTabDisplay]. Like [adaptiveNavEntry], Material
 * takes composable slots and iOS takes structured data, because native tab bar
 * items are built before the tab controller is shown.
 *
 * @param key the tab's root key, matching one of the state's tabs.
 * @param icon Material navigation item icon slot.
 * @param label Material navigation item label slot (`null` = icon only).
 * @param alwaysShowLabel Material: keep the label visible when unselected.
 * @param badge badge text shown on both platforms (`null` = no badge).
 * @param iosTitle title of the native iOS tab bar item.
 * @param iosImage image of the native iOS tab bar item.
 * @param iosSelectedImage image used while the tab is selected on iOS.
 */
@ExperimentalCalfNavigationApi
fun <T : NavKey> adaptiveTab(
    key: T,
    icon: @Composable () -> Unit,
    label: (@Composable () -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    badge: String? = null,
    iosTitle: String = "",
    iosImage: UIKitImage? = null,
    iosSelectedImage: UIKitImage? = null,
): AdaptiveTab<T> = AdaptiveTab(
    key = key,
    icon = icon,
    label = label,
    alwaysShowLabel = alwaysShowLabel,
    badge = badge,
    iosTitle = iosTitle,
    iosImage = iosImage,
    iosSelectedImage = iosSelectedImage,
)
