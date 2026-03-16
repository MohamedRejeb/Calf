package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerBackgroundLocationPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            minSdkVersion = 29,
        ),
    )
}
