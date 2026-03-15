package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.BluetoothPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry

internal actual fun registerBluetoothLePermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        BluetoothPermissionHelper()
    }
}

internal actual fun registerBluetoothScanPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        BluetoothPermissionHelper()
    }
}

internal actual fun registerBluetoothConnectPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        BluetoothPermissionHelper()
    }
}

internal actual fun registerBluetoothAdvertisePermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        BluetoothPermissionHelper()
    }
}
