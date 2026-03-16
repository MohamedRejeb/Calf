package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.BackgroundLocationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry

internal actual fun registerBackgroundLocationPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        BackgroundLocationPermissionHelper()
    }
}
