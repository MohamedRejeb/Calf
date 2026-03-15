package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.CalendarPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry

internal actual fun registerReadCalendarPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        CalendarPermissionHelper()
    }
}

internal actual fun registerWriteCalendarPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        CalendarPermissionHelper()
    }
}
