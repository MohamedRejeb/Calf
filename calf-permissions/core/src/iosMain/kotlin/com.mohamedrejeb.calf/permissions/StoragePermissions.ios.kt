package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.GrantedPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry

internal actual fun registerReadStoragePermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        GrantedPermissionHelper()
    }
}

internal actual fun registerWriteStoragePermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        GrantedPermissionHelper()
    }
}

internal actual fun registerReadAudioPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        GrantedPermissionHelper()
    }
}

internal actual fun registerCallPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        GrantedPermissionHelper()
    }
}
