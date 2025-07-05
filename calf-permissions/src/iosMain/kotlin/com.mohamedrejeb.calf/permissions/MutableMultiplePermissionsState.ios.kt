package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope

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
    onPermissionsResult: (Map<Permission, Boolean>) -> Unit
): MultiplePermissionsState {
    val scope = rememberCoroutineScope()
    val mutablePermissions = rememberMutablePermissionsState(permissions, scope, onPermissionsResult)
    // Refresh permissions when the lifecycle is resumed.
    PermissionsLifecycleCheckerEffect(mutablePermissions)
    val permissionStates =
        remember(permissions) {
            MutableMultiplePermissionsState(
                mutablePermissions = permissions.map {
                    MutablePermissionStateImpl(
                        permission = it,
                        onPermissionResult = { isOk ->
                            onPermissionsResult(mapOf(it to isOk))
                        },
                        scope = scope,
                    )
                }
            )
        }

    return permissionStates

}

@ExperimentalPermissionsApi
@Composable
private fun rememberMutablePermissionsState(
    permissions: List<Permission>,
    scope: CoroutineScope,
    onPermissionsResult: (Map<Permission, Boolean>) -> Unit
): List<MutablePermissionState> {
    // Create list of MutablePermissionState for each permission
    val mutablePermissions = remember(permissions) {
        return@remember permissions.map { permission ->
            MutablePermissionStateImpl(
                permission = permission,
                scope = scope,
                onPermissionResult = { isGranted ->
                    onPermissionsResult(mapOf(permission to isGranted))
                }
            )
        }
    }

    return mutablePermissions
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
    private val mutablePermissions: List<MutablePermissionState>
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