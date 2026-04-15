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
    // No-op on Android. Remote notification (push) doesn't require a separate
    // permission, the POST_NOTIFICATIONS permission covers both local and remote.
    // This permission is iOS-only (UNUserNotificationCenter).
}
