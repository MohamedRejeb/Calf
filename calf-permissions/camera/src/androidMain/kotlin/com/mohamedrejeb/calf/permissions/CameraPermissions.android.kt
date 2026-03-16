package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerCameraPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.CAMERA,
        ),
    )
}
