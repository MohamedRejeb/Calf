package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
internal expect fun rememberMutablePermissionState(
    permission: Permission,
    onPermissionResult: (Boolean) -> Unit = {}
): MutablePermissionState

/**
 * A mutable state object that can be used to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberMutablePermissionState].
 *
 * @param permission the permission to control and observe.
 * @param context to check the status of the [permission].
 * @param activity to check if the user should be presented with a rationale for [permission].
 */
@ExperimentalPermissionsApi
@Stable
internal expect class MutablePermissionState internal constructor(
    permission: Permission,
) : PermissionState {

    override val permission: Permission

    override var status: PermissionStatus

    override fun launchPermissionRequest()

    override fun openAppSettings()

    internal fun refreshPermissionStatus()
}