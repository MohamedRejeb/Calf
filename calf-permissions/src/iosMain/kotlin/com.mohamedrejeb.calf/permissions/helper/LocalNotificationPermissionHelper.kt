package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter

internal class LocalNotificationPermissionHelper : PermissionHelper {

    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral -> onPermissionResult(true)
                UNAuthorizationStatusNotDetermined -> {
                    notificationCenter.requestAuthorizationWithOptions(
                        UNAuthorizationOptionSound.or(UNAuthorizationOptionAlert).or(UNAuthorizationOptionBadge)
                    ) { isOk, error ->
                        if (isOk && error == null) {
                            onPermissionResult(true)
                        } else {
                            onPermissionResult(false)
                        }
                    }
                }
                else -> onPermissionResult(false)
            }
        }
    }

    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral -> onPermissionResult(PermissionStatus.Granted)
                else -> onPermissionResult(PermissionStatus.Denied(false))
            }
        }
    }
}