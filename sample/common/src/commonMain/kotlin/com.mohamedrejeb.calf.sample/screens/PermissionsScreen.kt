package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Notification
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.RecordAudio
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberMultiplePermissionsState
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.ui.button.AdaptiveButton

private const val USAGE = """
val camera = rememberPermissionState(Permission.Camera) { granted ->
    if (granted) openCamera()
}

when {
    camera.status.isGranted -> openCamera()
    camera.status.shouldShowRationale -> showWhyWeNeedTheCamera()
    else -> camera.launchPermissionRequest()
}
"""

@Composable
fun PermissionScreen(navigateBack: () -> Unit) {
    SampleScreenScaffold(title = Screen.Permission.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.Permission)
            VideoCallSection()
            PermissionGroups.forEach { group ->
                PermissionGroupSection(group)
            }
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

/** Two permissions asked together, the way a video call app would. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun VideoCallSection() {
    val permissions = listOf(Permission.Camera, Permission.RecordAudio)
    val state = rememberMultiplePermissionsState(permissions = permissions)
    DemoSection(
        title = "Video call",
        footer = "rememberMultiplePermissionsState asks for the camera and the microphone in one request.",
    ) {
        DemoCard {
            ValueRow(label = "All granted", value = yesNo(state.allPermissionsGranted))
            ValueRow(label = "Show rationale", value = yesNo(state.shouldShowRationale))
            ValueRow(
                label = "Revoked",
                value = state.revokedPermissions
                    .joinToString { it.permission.name }
                    .ifEmpty { "None" },
            )
        }
        AdaptiveButton(
            onClick = { state.launchMultiplePermissionRequest() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ask for camera and microphone")
        }
    }
}

@Composable
private fun PermissionGroupSection(group: PermissionGroup) {
    DemoSection(title = group.title, footer = group.footer) {
        DemoListCard {
            group.items.forEachIndexed { index, item ->
                if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                PermissionRow(item)
            }
            if (group.showsAppSettings) {
                DemoDivider(startIndent = DividerIndentWithIcon)
                AppSettingsRow()
            }
        }
    }
}

/** One permission: its status, and a tap asks for it while it is not granted. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRow(item: PermissionItem) {
    val state = rememberPermissionState(permission = item.permission)
    DemoRow(
        title = item.title,
        subtitle = item.permission.name,
        leading = { IconTile(icon = item.icon, color = item.tint) },
        trailing = { PermissionStatusLabel(state.status) },
        onClick = if (state.status.isGranted) null else ({ state.launchPermissionRequest() }),
    )
}

/**
 * Opens the app's page in the system settings, where a permission denied for good can still be
 * turned on.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AppSettingsRow() {
    val notification = rememberPermissionState(permission = Permission.Notification)
    DemoRow(
        title = "Open notification settings",
        subtitle = "openAppSettings()",
        leading = { IconTile(icon = Icons.Outlined.Settings, color = TileIndigo) },
        onClick = { notification.openAppSettings() },
    )
}

private fun yesNo(value: Boolean): String = if (value) "Yes" else "No"
