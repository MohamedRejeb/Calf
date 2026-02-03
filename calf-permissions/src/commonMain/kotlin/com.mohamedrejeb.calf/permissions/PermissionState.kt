package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode

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
    onPermissionResult: (Boolean) -> Unit = {},
): PermissionState {
    val isInspection = LocalInspectionMode.current

    return if (isInspection)
        rememberPreviewPermissionState(permission)
    else
        rememberMutablePermissionState(permission, onPermissionResult)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberPreviewPermissionState(
    permission: Permission
): PermissionState = remember {
    object : PermissionState {
        override val permission: Permission = permission
        override var status: PermissionStatus = PermissionStatus.Granted
        override fun launchPermissionRequest() {}
        override fun openAppSettings() {}
    }
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
    var status: PermissionStatus

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

    /**
     * Open the app settings page.
     *
     * This should always be triggered from non-composable scope, for example, from a side-effect
     * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
     */
    fun openAppSettings()
}

/**
 * Enum representing the different supported permissions.
 */
enum class Permission {
    Call,
    Camera,
    Gallery,
    ReadStorage,
    WriteStorage,
    ReadImage,
    ReadVideo,
    ReadAudio,
    FineLocation,
    CoarseLocation,
    BackgroundLocation,
    Notification,
    RemoteNotification,
    RecordAudio,
    BluetoothLe,
    BluetoothScan,
    BluetoothConnect,
    BluetoothAdvertise,
    ReadContacts,
    WriteContacts,
    ReadCalendar,
    WriteCalendar,
    WifiAccessState,
    WifiChangeState,
    WifiNearbyDevices,
    SendSms,
    ReceiveSms,
    ReadSms
}
