package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerNotificationPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.POST_NOTIFICATIONS,
            minSdkVersion = 33,
        ),
    )
}

internal actual fun registerRemoteNotificationPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.RECEIVE_BOOT_COMPLETED,
            alwaysGranted = true,
        ),
    )
}
