package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.LocationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry

internal actual fun registerFineLocationPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        LocationPermissionHelper()
    }
}

internal actual fun registerCoarseLocationPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        LocationPermissionHelper()
    }
}

