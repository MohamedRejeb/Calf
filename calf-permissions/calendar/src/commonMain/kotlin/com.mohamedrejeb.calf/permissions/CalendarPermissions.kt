package com.mohamedrejeb.calf.permissions

val Permission.Companion.ReadCalendar: Permission
    get() = PermissionReadCalendar

val Permission.Companion.WriteCalendar: Permission
    get() = PermissionWriteCalendar

internal expect fun registerReadCalendarPermission(permission: Permission)

internal expect fun registerWriteCalendarPermission(permission: Permission)

internal object PermissionReadCalendar : Permission("ReadCalendar") {
    init {
        registerReadCalendarPermission(this)
    }
}

internal object PermissionWriteCalendar : Permission("WriteCalendar") {
    init {
        registerWriteCalendarPermission(this)
    }
}
