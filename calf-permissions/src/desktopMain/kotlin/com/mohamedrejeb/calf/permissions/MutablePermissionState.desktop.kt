package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Creates a [MutablePermissionState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permission the permission to control and observe.
 * @param onPermissionResult will be called with whether or not the user granted the permission
 *  after [PermissionState.launchPermissionRequest] is called.
 */
@ExperimentalPermissionsApi
@Composable
internal actual fun rememberMutablePermissionState(
    permission: Permission,
    onPermissionResult: (Boolean) -> Unit
): MutablePermissionState {
    return remember(permission) {
        MutablePermissionState(permission)
    }
}

/**
 * A mutable state object that can be used to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberMutablePermissionState].
 *
 * @param permission the permission to control and observe.
 */
@ExperimentalPermissionsApi
@Stable
internal actual class MutablePermissionState actual constructor(
    actual override val permission: Permission,
) : PermissionState {

    actual override var status: PermissionStatus by mutableStateOf(getPermissionStatus())

    actual override fun launchPermissionRequest() {}

    actual override fun openAppSettings() {}

    internal actual fun refreshPermissionStatus() {}

    private fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus.Denied(false)
    }
}