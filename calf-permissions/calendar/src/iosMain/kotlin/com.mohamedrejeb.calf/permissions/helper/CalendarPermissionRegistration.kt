package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val calendarRegistration = run {
    PermissionDelegateRegistry.register(Permission.ReadCalendar) {
        CalendarPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.WriteCalendar) {
        CalendarPermissionHelper()
    }
}
