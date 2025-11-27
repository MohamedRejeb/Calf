package com.mohamedrejeb.calf.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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

/**
 * Find the closest Activity in a given Context.
 */
internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

internal fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

internal fun Activity.shouldShowRationale(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}

internal fun Permission.toAndroidPermission(): String {
    return when (this) {
        Permission.Call -> Manifest.permission.CALL_PHONE
        Permission.Camera -> Manifest.permission.CAMERA
        Permission.Gallery ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_EXTERNAL_STORAGE
            else
                ""

        Permission.ReadStorage ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_EXTERNAL_STORAGE
            else
                ""

        Permission.WriteStorage ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            else
                ""

        Permission.ReadImage ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
                ""

        Permission.ReadVideo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_VIDEO
            else
                ""

        Permission.ReadAudio ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_AUDIO
            else
                ""

        Permission.FineLocation -> Manifest.permission.ACCESS_FINE_LOCATION
        Permission.CoarseLocation -> Manifest.permission.ACCESS_COARSE_LOCATION
        Permission.BackgroundLocation -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        else
            ""

        Permission.RemoteNotification -> Manifest.permission.RECEIVE_BOOT_COMPLETED
        Permission.RecordAudio -> Manifest.permission.RECORD_AUDIO
        Permission.BluetoothLe -> Manifest.permission.BLUETOOTH
        Permission.BluetoothScan ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                Manifest.permission.BLUETOOTH_SCAN
            else
                ""

        Permission.BluetoothConnect ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                Manifest.permission.BLUETOOTH_CONNECT
            else
                ""

        Permission.BluetoothAdvertise ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                Manifest.permission.BLUETOOTH_ADVERTISE
            else
                ""

        Permission.Notification ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.POST_NOTIFICATIONS
            else
                ""

        Permission.ReadContacts -> Manifest.permission.READ_CONTACTS
        Permission.WriteContacts -> Manifest.permission.WRITE_CONTACTS
        Permission.ReadCalendar -> Manifest.permission.READ_CALENDAR
        Permission.WriteCalendar -> Manifest.permission.WRITE_CALENDAR
        Permission.WifiAccessState -> Manifest.permission.ACCESS_WIFI_STATE
        Permission.WifiChangeState -> Manifest.permission.CHANGE_WIFI_STATE

        Permission.WifiNearbyDevices ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.NEARBY_WIFI_DEVICES
            else
                ""
    }

}

internal fun Permission.isAlwaysGranted(): Boolean =
    when {
        this == Permission.RemoteNotification ->
            true

        else -> false
    }

internal fun getPermissionFromAndroidPermission(androidPermission: String): Permission? {
    return when (androidPermission) {
        Manifest.permission.CALL_PHONE -> Permission.Call
        Manifest.permission.CAMERA -> Permission.Camera
        Manifest.permission.READ_EXTERNAL_STORAGE -> Permission.ReadStorage
        Manifest.permission.WRITE_EXTERNAL_STORAGE -> Permission.WriteStorage
        Manifest.permission.READ_MEDIA_IMAGES -> Permission.ReadImage
        Manifest.permission.READ_MEDIA_VIDEO -> Permission.ReadVideo
        Manifest.permission.READ_MEDIA_AUDIO -> Permission.ReadAudio
        Manifest.permission.ACCESS_FINE_LOCATION -> Permission.FineLocation
        Manifest.permission.ACCESS_COARSE_LOCATION -> Permission.CoarseLocation
        Manifest.permission.POST_NOTIFICATIONS -> Permission.Notification
        Manifest.permission.RECEIVE_BOOT_COMPLETED -> Permission.RemoteNotification
        Manifest.permission.RECORD_AUDIO -> Permission.RecordAudio
        Manifest.permission.BLUETOOTH -> Permission.BluetoothLe
        Manifest.permission.BLUETOOTH_SCAN -> Permission.BluetoothScan
        Manifest.permission.BLUETOOTH_CONNECT -> Permission.BluetoothConnect
        Manifest.permission.BLUETOOTH_ADVERTISE -> Permission.BluetoothAdvertise
        Manifest.permission.READ_CONTACTS -> Permission.ReadContacts
        Manifest.permission.WRITE_CONTACTS -> Permission.WriteContacts
        Manifest.permission.ACCESS_WIFI_STATE -> Permission.WifiAccessState
        Manifest.permission.CHANGE_WIFI_STATE -> Permission.WifiChangeState
        Manifest.permission.NEARBY_WIFI_DEVICES -> Permission.WifiNearbyDevices
        else -> null
    }
}
