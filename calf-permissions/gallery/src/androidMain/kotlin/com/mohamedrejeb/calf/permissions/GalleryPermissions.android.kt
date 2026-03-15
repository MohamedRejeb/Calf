package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerGalleryPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.READ_EXTERNAL_STORAGE,
            maxSdkVersion = 32,
        ),
    )
}

internal actual fun registerReadImagePermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.READ_MEDIA_IMAGES,
            minSdkVersion = 33,
        ),
    )
}

internal actual fun registerReadVideoPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.READ_MEDIA_VIDEO,
            minSdkVersion = 33,
        ),
    )
}
