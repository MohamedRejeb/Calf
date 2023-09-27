package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * Creates a [PermissionState] that is remembered across compositions.
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
fun rememberPermissionState(
    permission: Permission,
    onPermissionResult: (Boolean) -> Unit = {}
): PermissionState {
    return rememberMutablePermissionState(permission, onPermissionResult)
}

/**
 * A state object that can be hoisted to control and observe [permission] status changes.
 *
 * In most cases, this will be created via [rememberPermissionState].
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 */
@ExperimentalPermissionsApi
@Stable
interface PermissionState {

    /**
     * The permission to control and observe.
     */
    val permission: Permission

    /**
     * [permission]'s status
     */
    val status: PermissionStatus

    /**
     * Request the [permission] to the user.
     *
     * This should always be triggered from non-composable scope, for example, from a side-effect
     * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
     *
     * This triggers a system dialog that asks the user to grant or revoke the permission.
     * Note that this dialog might not appear on the screen if the user doesn't want to be asked
     * again or has denied the permission multiple times.
     * This behavior varies depending on the Android level API.
     */
    fun launchPermissionRequest()
}

/**
 * Enum representing the different supported permissions.
 */
enum class Permission {
    Camera,
    Gallery,
    ReadStorage,
    WriteStorage,
    FileLocation,
    CoarseLocation,
    RemoteNotification,
    RecordAudio,
    BluetoothLe,
    BluetoothScan,
    BluetoothConnect,
    BluetoothAdvertise,
}