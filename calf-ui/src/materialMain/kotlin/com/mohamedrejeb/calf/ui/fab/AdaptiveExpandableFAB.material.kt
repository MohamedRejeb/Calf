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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi

/**
 * Material3 implementation of [AdaptiveExpandableFAB].
 *
 * Displays a main FAB that, when expanded, reveals additional action items
 * with an animated transition. The `ios*` parameters are ignored on Material platforms.
 */
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
