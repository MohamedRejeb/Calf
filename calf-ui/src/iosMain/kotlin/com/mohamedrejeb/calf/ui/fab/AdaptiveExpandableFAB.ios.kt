package com.mohamedrejeb.calf.ui.fab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove

/**
 * iOS implementation of [AdaptiveExpandableFAB].
 *
 * On iOS 26+, uses native UIButton instances with Liquid Glass effects inside a container view.
 * On older iOS versions, falls back to Material-style Compose implementation.
 */
@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalCalfUiApi
@Composable
internal actual fun AdaptiveExpandableFAB(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    mainIcon: ImageVector,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    iosMainImage: UIKitExpandableFABItem,
    iosItems: List<UIKitExpandableFABItem>,
    iosOnItemSelected: (Int) -> Unit,
    items: @Composable () -> Unit,
) {
    val isLiquidGlassAvailable = remember { isIOS26OrAbove() }

    if (isLiquidGlassAvailable && iosItems.isNotEmpty()) {
        LiquidGlassExpandableFAB(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            mainImage = iosMainImage,
            iosItems = iosItems,
            iosOnItemSelected = iosOnItemSelected,
        )
    } else {
        // Fallback to Material-style implementation for older iOS versions
        MaterialStyleExpandableFAB(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            mainIcon = mainIcon,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            items = items,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LiquidGlassExpandableFAB(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    mainImage: UIKitExpandableFABItem,
    iosItems: List<UIKitExpandableFABItem>,
    iosOnItemSelected: (Int) -> Unit,
) {
    val viewController = LocalUIViewController.current
    val onExpandedChangeState by rememberUpdatedState(onExpandedChange)
    val onItemSelectedState by rememberUpdatedState(iosOnItemSelected)

    val fabManager = remember {
        ExpandableFABManager(
            onExpandedChange = { onExpandedChangeState(it) },
            onItemSelected = { onItemSelectedState(it) },
        )
    }

    DisposableEffect(viewController) {
        fabManager.attachTo(viewController.view)

        onDispose {
            fabManager.detach()
        }
    }

    LaunchedEffect(mainImage, iosItems) {
        fabManager.updateItems(mainImage, iosItems)
    }

    LaunchedEffect(expanded) {
        fabManager.setExpanded(expanded)
    }
}

@Composable
private fun MaterialStyleExpandableFAB(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    mainIcon: ImageVector,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    items: @Composable () -> Unit,
) {
    val resolvedContainerColor =
        if (containerColor == Color.Unspecified) FloatingActionButtonDefaults.containerColor
        else containerColor
    val resolvedContentColor =
        if (contentColor == Color.Unspecified)
            MaterialTheme.colorScheme.contentColorFor(resolvedContainerColor)
        else contentColor

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation",
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                items()
            }
        }

        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = resolvedContainerColor,
            contentColor = resolvedContentColor,
        ) {
            Icon(
                imageVector = mainIcon,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}
