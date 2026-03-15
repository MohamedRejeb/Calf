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

internal actual fun registerRecordAudioPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.RECORD_AUDIO,
        ),
    )
}
