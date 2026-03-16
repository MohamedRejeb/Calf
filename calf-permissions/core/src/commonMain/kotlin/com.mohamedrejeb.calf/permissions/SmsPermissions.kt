package com.mohamedrejeb.calf.permissions

val Permission.Companion.ReadSms: Permission
    get() = PermissionReadSms

val Permission.Companion.ReceiveSms: Permission
    get() = PermissionReceiveSms

val Permission.Companion.SendSms: Permission
    get() = PermissionSendSms

internal expect fun registerReadSmsPermission(permission: Permission)

internal expect fun registerReceiveSmsPermission(permission: Permission)

internal expect fun registerSendSmsPermission(permission: Permission)

internal object PermissionReadSms : Permission("ReadSms") {
    init {
        registerReadSmsPermission(this)
    }
}

internal object PermissionReceiveSms : Permission("ReceiveSms") {
    init {
        registerReceiveSmsPermission(this)
    }
}

internal object PermissionSendSms : Permission("SendSms") {
    init {
        registerSendSmsPermission(this)
    }
}
