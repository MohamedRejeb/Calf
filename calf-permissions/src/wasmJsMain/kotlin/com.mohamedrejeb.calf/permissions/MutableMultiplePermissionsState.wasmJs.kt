package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permissions the permissions to control and observe.
 * @param onPermissionsResult will be called with whether or not the user granted the permissions
 *  after [MultiplePermissionsState.launchMultiplePermissionRequest] is called.
 */
@ExperimentalPermissionsApi
@Composable
internal actual fun rememberMutableMultiplePermissionsState(
    permissions: List<Permission>,
    onPermissionsResult: (Map<Permission, Boolean>) -> Unit,
): MultiplePermissionsState {
    // Create mutable permissions that can be requested individually
    val mutablePermissions =
        permissions.map { permission ->
            rememberMutablePermissionState(permission) { granted ->
                onPermissionsResult(mapOf(permission to granted))
            }
        }

    val multiplePermissionsState =
        remember(permissions) {
            MutableMultiplePermissionsState(mutablePermissions)
        }

    return multiplePermissionsState
}

/**
 * A state object that can be hoisted to control and observe multiple permission status changes.
 *
 * In most cases, this will be created via [rememberMutableMultiplePermissionsState].
 *
 * @param mutablePermissions list of mutable permissions to control and observe.
 */
@ExperimentalPermissionsApi
@Stable
internal actual class MutableMultiplePermissionsState actual constructor(
    private val mutablePermissions: List<MutablePermissionState>,
) : MultiplePermissionsState {
    actual override val permissions: List<PermissionState> = mutablePermissions

    actual override val revokedPermissions: List<PermissionState> by derivedStateOf {
        permissions.filter { it.status != PermissionStatus.Granted }
    }

    actual override val allPermissionsGranted: Boolean by derivedStateOf {
        permissions.all { it.status.isGranted } || // Up to date when the lifecycle is resumed
            revokedPermissions.isEmpty() // Up to date when the user launches the action
    }

    actual override val shouldShowRationale: Boolean by derivedStateOf {
        permissions.any { it.status.shouldShowRationale }
    }

    actual override fun launchMultiplePermissionRequest() {
        // Launch the permission request
    }

    internal actual fun updatePermissionsStatus(permissionsStatus: Map<Permission, Boolean>) {
        // Update all permissions with the result
        for (permission in permissionsStatus.keys) {
            mutablePermissions.firstOrNull { it.permission == permission }?.apply {
                permissionsStatus[permission]?.let {
                    this.refreshPermissionStatus()
                }
            }
        }
    }
}
