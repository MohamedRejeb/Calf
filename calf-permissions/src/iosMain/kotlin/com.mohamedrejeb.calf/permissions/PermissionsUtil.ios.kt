package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mohamedrejeb.calf.permissions.helper.AVCapturePermissionHelper
import com.mohamedrejeb.calf.permissions.helper.BluetoothPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.CalendarPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.ContactPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.GalleryPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.GrantedPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.LocalNotificationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.LocationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.NotGrantedPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionHelper
import com.mohamedrejeb.calf.permissions.helper.RemoteNotificationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.WifiPermissionHelper
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo

/**
 * Effect that updates the `hasPermission` state of a revoked [MutablePermissionState] permission
 * when the lifecycle gets called with [lifecycleEvent].
 */
@ExperimentalPermissionsApi
@Composable
internal fun PermissionLifecycleCheckerEffect(
    permissionState: MutablePermissionState,
    lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
) {
    // Check if the permission was granted when the lifecycle is resumed.
    // The user might've gone to the Settings screen and granted the permission.
    val permissionCheckerObserver =
        remember(permissionState) {
            LifecycleEventObserver { _, event ->
                if (event == lifecycleEvent) {
                    // If the permission is revoked, check again.
                    // We don't check if the permission was denied as that triggers a process restart.
                    if (permissionState.status != PermissionStatus.Granted) {
                        permissionState.refreshPermissionStatus()
                    }
                }
            }
        }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, permissionCheckerObserver) {
        lifecycle.addObserver(permissionCheckerObserver)
        onDispose { lifecycle.removeObserver(permissionCheckerObserver) }
    }
}

/**
 * Effect that updates the `hasPermission` state of a list of permissions
 * when the lifecycle gets called with [lifecycleEvent] and the permission is revoked.
 */
@ExperimentalPermissionsApi
@Composable
internal fun PermissionsLifecycleCheckerEffect(
    permissions: List<MutablePermissionState>,
    lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
) {
    // Check if the permission was granted when the lifecycle is resumed.
    // The user might've gone to the Settings screen and granted the permission.
    val permissionsCheckerObserver =
        remember(permissions) {
            LifecycleEventObserver { _, event ->
                if (event == lifecycleEvent) {
                    for (permission in permissions) {
                        // If the permission is revoked, check again. We don't check if the permission
                        // was denied as that triggers a process restart.
                        if (permission.status != PermissionStatus.Granted) {
                            permission.refreshPermissionStatus()
                        }
                    }
                }
            }
        }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, permissionsCheckerObserver) {
        lifecycle.addObserver(permissionsCheckerObserver)
        onDispose { lifecycle.removeObserver(permissionsCheckerObserver) }
    }
}

internal fun Permission.getPermissionDelegate(): PermissionHelper {
    return when (this) {
        Permission.Camera ->
            AVCapturePermissionHelper(AVMediaTypeVideo)

        Permission.Gallery,
        Permission.ReadImage,
        Permission.ReadVideo,
            ->
            GalleryPermissionHelper()

        Permission.ReadStorage,
        Permission.WriteStorage,
        Permission.ReadAudio,
        Permission.Call,
        Permission.SendSms
            ->
            GrantedPermissionHelper()

        Permission.FineLocation,
        Permission.CoarseLocation,
        Permission.BackgroundLocation,
            ->
            LocationPermissionHelper()

        Permission.Notification ->
            LocalNotificationPermissionHelper()

        Permission.RemoteNotification ->
            RemoteNotificationPermissionHelper()

        Permission.RecordAudio ->
            AVCapturePermissionHelper(AVMediaTypeAudio)

        Permission.BluetoothLe,
        Permission.BluetoothScan,
        Permission.BluetoothConnect,
        Permission.BluetoothAdvertise,
            ->
            BluetoothPermissionHelper()

        Permission.ReadContacts -> ContactPermissionHelper()
        Permission.WriteContacts -> ContactPermissionHelper()
        Permission.ReadCalendar -> CalendarPermissionHelper()
        Permission.WriteCalendar -> CalendarPermissionHelper()

        Permission.WifiAccessState,
        Permission.WifiChangeState,
        Permission.WifiNearbyDevices,
            ->
            WifiPermissionHelper()

        Permission.ReadSms,
        Permission.ReceiveSms -> NotGrantedPermissionHelper()
    }
}
