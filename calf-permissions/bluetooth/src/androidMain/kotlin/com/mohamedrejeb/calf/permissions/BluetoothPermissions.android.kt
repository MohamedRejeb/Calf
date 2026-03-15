package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerBluetoothLePermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.BLUETOOTH,
        ),
    )
}

internal actual fun registerBluetoothScanPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.BLUETOOTH_SCAN,
            minSdkVersion = 31,
        ),
    )
}

internal actual fun registerBluetoothConnectPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.BLUETOOTH_CONNECT,
            minSdkVersion = 31,
        ),
    )
}

internal actual fun registerBluetoothAdvertisePermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.BLUETOOTH_ADVERTISE,
            minSdkVersion = 31,
        ),
    )
}
