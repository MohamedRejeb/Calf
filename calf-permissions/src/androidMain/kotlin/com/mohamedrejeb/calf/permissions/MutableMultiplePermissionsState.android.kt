package com.mohamedrejeb.calf.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
    onPermissionsResult: (Map<Permission, PermissionStatus>) -> Unit
): MultiplePermissionsState {
    // Create mutable permissions that can be requested individually
    val mutablePermissions = rememberMutablePermissionsState(permissions, onPermissionsResult)
    // Refresh permissions when the lifecycle is resumed.
    PermissionsLifecycleCheckerEffect(mutablePermissions)

    val multiplePermissionsState = remember(permissions) {
        MutableMultiplePermissionsState(mutablePermissions)
    }

    // Remember RequestMultiplePermissions launcher and assign it to multiplePermissionsState
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val result = permissionsResult
            .mapKeys { getPermissionFromAndroidPermission(it.key) }
            .filter { it.key != null }
            .mapKeys { it.key!! }
        multiplePermissionsState.updatePermissionsStatus(result)
        val permissionWithStatus = multiplePermissionsState.permissions.associate  {
             Pair(it.permission, it.status)
        }
        onPermissionsResult(permissionWithStatus)
    }
    DisposableEffect(multiplePermissionsState, launcher) {
        multiplePermissionsState.launcher = launcher
        onDispose {
            multiplePermissionsState.launcher = null
        }
    }

    return multiplePermissionsState
}

@ExperimentalPermissionsApi
@Composable
private fun rememberMutablePermissionsState(
    permissions: List<Permission>,
    onPermissionsResult: (Map<Permission, PermissionStatus>) -> Unit
): List<MutablePermissionState> {
    // Create list of MutablePermissionState for each permission
    val context = LocalContext.current
    val activity = context.findActivity()
    val mutablePermissions = remember(permissions) {
        return@remember permissions.map {  permission ->
            MutablePermissionStateImpl(
                permission,
                context,
                activity,
            ) { permissionState ->
                onPermissionsResult(mapOf(permission to permissionState))
            }
        }
    }
    // Update each permission with its own launcher
    for (permissionState in mutablePermissions) {
        key(permissionState.permission) {
            // Remember launcher and assign it to the permissionState
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                permissionState.refreshPermissionStatus()
            }
            DisposableEffect(launcher) {
                permissionState.launcher = launcher
                onDispose {
                    permissionState.launcher = null
                }
            }
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
        launcher?.launch(
            permissions.map { it.permission.toAndroidPermission() }.toTypedArray()
        ) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
    }

    internal var launcher: ActivityResultLauncher<Array<String>>? = null

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