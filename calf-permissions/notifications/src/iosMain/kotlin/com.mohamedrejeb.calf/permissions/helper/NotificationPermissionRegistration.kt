package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val notificationRegistration = run {
    PermissionDelegateRegistry.register(Permission.Notification) {
        LocalNotificationPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.RemoteNotification) {
        RemoteNotificationPermissionHelper()
    }
}
