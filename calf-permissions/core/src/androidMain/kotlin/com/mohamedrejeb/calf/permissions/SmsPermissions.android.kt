package com.mohamedrejeb.calf.permissions

import android.Manifest

internal actual fun registerReadSmsPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.READ_SMS,
        ),
    )
}

internal actual fun registerReceiveSmsPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.RECEIVE_SMS,
        ),
    )
}

internal actual fun registerSendSmsPermission(permission: Permission) {
    AndroidPermissionRegistry.register(
        permission,
        AndroidPermissionMapping(
            permissionString = Manifest.permission.SEND_SMS,
        ),
    )
}
