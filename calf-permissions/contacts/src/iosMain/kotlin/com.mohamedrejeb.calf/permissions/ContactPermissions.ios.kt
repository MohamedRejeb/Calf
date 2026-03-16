package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.ContactPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry

internal actual fun registerReadContactsPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        ContactPermissionHelper()
    }
}

internal actual fun registerWriteContactsPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        ContactPermissionHelper()
    }
}
