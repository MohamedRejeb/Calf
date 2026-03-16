package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry
import com.mohamedrejeb.calf.permissions.helper.WifiPermissionHelper

internal actual fun registerWifiAccessStatePermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        WifiPermissionHelper()
    }
}

internal actual fun registerWifiChangeStatePermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        WifiPermissionHelper()
    }
}

internal actual fun registerWifiNearbyDevicesPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        WifiPermissionHelper()
    }
}
