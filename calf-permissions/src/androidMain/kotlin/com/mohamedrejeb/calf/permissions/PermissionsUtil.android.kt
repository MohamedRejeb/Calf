package com.mohamedrejeb.calf.permissions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
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
    lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME
) {
    // Check if the permission was granted when the lifecycle is resumed.
    // The user might've gone to the Settings screen and granted the permission.
    val permissionCheckerObserver = remember(permissionState) {
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
    val lifecycle = LocalLifecycleOwner.current.lifecycle
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
    lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME
) {
    // Check if the permission was granted when the lifecycle is resumed.
    // The user might've gone to the Settings screen and granted the permission.
    val permissionsCheckerObserver = remember(permissions) {
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
    val lifecycle = LocalLifecycleOwner.current.lifecycle
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
        Permission.Camera -> android.Manifest.permission.CAMERA
        Permission.Gallery -> android.Manifest.permission.READ_EXTERNAL_STORAGE
        Permission.ReadStorage -> android.Manifest.permission.READ_EXTERNAL_STORAGE
        Permission.WriteStorage -> android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        Permission.FineLocation -> android.Manifest.permission.ACCESS_FINE_LOCATION
        Permission.CoarseLocation -> android.Manifest.permission.ACCESS_COARSE_LOCATION
        Permission.RemoteNotification -> android.Manifest.permission.RECEIVE_BOOT_COMPLETED
        Permission.RecordAudio -> android.Manifest.permission.RECORD_AUDIO
        Permission.BluetoothLe -> android.Manifest.permission.BLUETOOTH
        Permission.BluetoothScan -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.Manifest.permission.BLUETOOTH_SCAN
        } else ""
        Permission.BluetoothConnect -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.Manifest.permission.BLUETOOTH_CONNECT
        } else ""
        Permission.BluetoothAdvertise -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.Manifest.permission.BLUETOOTH_ADVERTISE
        } else ""
    }
}

internal fun Permission.isAlwaysGranted(): Boolean = when(this) {
    Permission.Gallery,
    Permission.ReadStorage,
    Permission.WriteStorage,
    Permission.RemoteNotification -> true
    else -> false
}

internal fun getPermissionFromAndroidPermission(androidPermission: String): Permission? {
    return when (androidPermission) {
        android.Manifest.permission.CAMERA -> Permission.Camera
        android.Manifest.permission.READ_EXTERNAL_STORAGE -> Permission.ReadStorage
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> Permission.WriteStorage
        android.Manifest.permission.ACCESS_FINE_LOCATION -> Permission.FineLocation
        android.Manifest.permission.ACCESS_COARSE_LOCATION -> Permission.CoarseLocation
        android.Manifest.permission.RECEIVE_BOOT_COMPLETED -> Permission.RemoteNotification
        android.Manifest.permission.RECORD_AUDIO -> Permission.RecordAudio
        android.Manifest.permission.BLUETOOTH -> Permission.BluetoothLe
        android.Manifest.permission.BLUETOOTH_SCAN -> Permission.BluetoothScan
        android.Manifest.permission.BLUETOOTH_CONNECT -> Permission.BluetoothConnect
        android.Manifest.permission.BLUETOOTH_ADVERTISE -> Permission.BluetoothAdvertise
        else -> null
    }
}