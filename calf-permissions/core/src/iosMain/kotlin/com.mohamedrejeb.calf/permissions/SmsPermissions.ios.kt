package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.GrantedPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.NotGrantedPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry

internal actual fun registerReadSmsPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        NotGrantedPermissionHelper()
    }
}

internal actual fun registerReceiveSmsPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        NotGrantedPermissionHelper()
    }
}

internal actual fun registerSendSmsPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        GrantedPermissionHelper()
    }
}
