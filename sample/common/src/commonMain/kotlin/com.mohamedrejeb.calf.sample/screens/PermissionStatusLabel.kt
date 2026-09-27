package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.sample.components.StatusLabel
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal

/**
 * "● Allowed": a permission's status as a [StatusLabel], the trailing content of a row. A denial
 * the system wants explained ([PermissionStatus.Denied.shouldShowRationale]) reads differently
 * from one it does not, which is either not asked yet or denied for good.
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

