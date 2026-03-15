package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerWifiAccessStatePermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.ACCESS_WIFI_STATE,
        ),
    )
}

internal actual fun registerWifiChangeStatePermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.CHANGE_WIFI_STATE,
        ),
    )
}

internal actual fun registerWifiNearbyDevicesPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.NEARBY_WIFI_DEVICES,
            minSdkVersion = 33,
        ),
    )
}
