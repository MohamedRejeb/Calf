package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.LocalNotificationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry
import com.mohamedrejeb.calf.permissions.helper.RemoteNotificationPermissionHelper

internal actual fun registerNotificationPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        LocalNotificationPermissionHelper()
    }
}

internal actual fun registerRemoteNotificationPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        RemoteNotificationPermissionHelper()
    }
}
