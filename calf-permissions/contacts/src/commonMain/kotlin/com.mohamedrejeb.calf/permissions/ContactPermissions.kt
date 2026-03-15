package com.mohamedrejeb.calf.permissions

val Permission.Companion.ReadContacts: Permission
    get() = PermissionReadContacts

val Permission.Companion.WriteContacts: Permission
    get() = PermissionWriteContacts

internal expect fun registerReadContactsPermission(permission: Permission)

internal expect fun registerWriteContactsPermission(permission: Permission)

internal object PermissionReadContacts : Permission("ReadContacts") {
    init {
        registerReadContactsPermission(this)
    }
}

internal object PermissionWriteContacts : Permission("WriteContacts") {
    init {
        registerWriteContactsPermission(this)
    }
}
