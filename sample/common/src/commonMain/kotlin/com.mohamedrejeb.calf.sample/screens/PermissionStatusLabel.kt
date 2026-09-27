package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.sample.components.StatusDot
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal

/**
 * "● Allowed": a permission's status as the trailing content of a row. A denial the system
 * wants explained ([PermissionStatus.Denied.shouldShowRationale]) reads differently from one it
 * does not, which is either not asked yet or denied for good.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PermissionStatusLabel(
    status: PermissionStatus,
    modifier: Modifier = Modifier,
) {
    val (label, color) = when (status) {
        PermissionStatus.Granted -> "Allowed" to TileTeal
        is PermissionStatus.Denied ->
            if (status.shouldShowRationale) "Denied" to TileAmber
            else "Not allowed" to MaterialTheme.colorScheme.outline
    }
    StatusLabel(label = label, color = color, modifier = modifier)
}

/** A [StatusDot] with its label, for the trailing side of a row. */
@Composable
internal fun StatusLabel(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = modifier,
    ) {
        StatusDot(color = color)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
