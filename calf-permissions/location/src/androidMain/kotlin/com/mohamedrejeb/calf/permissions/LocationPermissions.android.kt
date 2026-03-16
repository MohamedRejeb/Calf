package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerFineLocationPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.ACCESS_FINE_LOCATION,
        ),
    )
}

internal actual fun registerCoarseLocationPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
    )
}
