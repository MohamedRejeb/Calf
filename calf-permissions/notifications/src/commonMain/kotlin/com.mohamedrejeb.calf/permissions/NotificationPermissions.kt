package com.mohamedrejeb.calf.permissions

val Permission.Companion.Notification: Permission
    get() = PermissionNotification

val Permission.Companion.RemoteNotification: Permission
    get() = PermissionRemoteNotification

internal expect fun registerNotificationPermission(permission: Permission)

internal expect fun registerRemoteNotificationPermission(permission: Permission)

internal object PermissionNotification : Permission("Notification") {
    init {
        registerNotificationPermission(this)
    }
}

internal object PermissionRemoteNotification : Permission("RemoteNotification") {
    init {
        registerRemoteNotificationPermission(this)
    }
}
