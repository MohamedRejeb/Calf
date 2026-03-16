package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerReadStoragePermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.READ_EXTERNAL_STORAGE,
            maxSdkVersion = 32,
        ),
    )
}

internal actual fun registerWriteStoragePermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.WRITE_EXTERNAL_STORAGE,
            maxSdkVersion = 28,
        ),
    )
}

internal actual fun registerReadAudioPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.READ_MEDIA_AUDIO,
            minSdkVersion = 33,
        ),
    )
}

internal actual fun registerCallPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.CALL_PHONE,
        ),
    )
}
