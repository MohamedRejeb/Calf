package com.mohamedrejeb.calf.ui.fab

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

/**
 * An adaptive expandable Floating Action Button that uses a Liquid Glass expanding button menu
 * on iOS 26+ and a Material-style animated expanding FAB on other platforms.
 *
 * When [expanded] is false, a single main FAB is displayed. When [expanded] is true, the FAB
 * expands to reveal additional action items.
 *
 * On iOS 26+, the items are rendered as [UIButton] instances inside a [UIGlassEffectContainerView],
 * creating the "liquid expanding" glass effect. On older iOS versions and other platforms, a
 * Material-style expanding FAB with animated item entries is used.
 *
 * @param expanded Whether the FAB menu is currently expanded.
 * @param onExpandedChange Callback invoked when the expanded state should change.
 * @param mainIcon The icon for the main FAB button (used on Material platforms).
 * @param modifier The modifier to be applied to the FAB.
 * @param containerColor The background color of the FAB on Material platforms.
 * @param contentColor The content/icon color of the FAB on Material platforms.
 * @param iosMainImage The image for the main FAB button on iOS. See [UIKitExpandableFABItem].
 * @param iosItems The list of expandable items for iOS. See [UIKitExpandableFABItem].
 * @param iosOnItemSelected Callback invoked when an iOS item is selected, with the item index.
 * @param items The content composable for expanded items on Material platforms. Typically a
 *   column of [SmallFloatingActionButton] or similar composables.
 */
@ExperimentalCalfUiApi
@Composable
expect fun AdaptiveExpandableFAB(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    mainIcon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    iosMainImage: UIKitExpandableFABItem = UIKitExpandableFABItem(title = ""),
    iosItems: List<UIKitExpandableFABItem> = emptyList(),
    iosOnItemSelected: (Int) -> Unit = {},
    items: @Composable () -> Unit = {},
)
