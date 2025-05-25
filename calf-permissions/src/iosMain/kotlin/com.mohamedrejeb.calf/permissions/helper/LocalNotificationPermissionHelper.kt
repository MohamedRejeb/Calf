package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter

internal class LocalNotificationPermissionHelper : PermissionHelper {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handleLaunchPermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                getCurrentNotificationCenter()
                    .requestAuthorizationWithOptions(
                        UNAuthorizationOptionSound
                            .or(UNAuthorizationOptionAlert)
                            .or(UNAuthorizationOptionBadge)
                    ) { isOk, error ->
                        if (isOk && error == null)
                            onPermissionResult(true)
                        else
                            onPermissionResult(false)
                    }
            }
        )
    }

    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val notificationCenter = getCurrentNotificationCenter()
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral,
                    ->
                    onPermissionResult(PermissionStatus.Granted)

                UNAuthorizationStatusNotDetermined ->
                    onPermissionResult(PermissionStatus.Denied(shouldShowRationale = false))

                UNAuthorizationStatusDenied ->
                    onPermissionResult(PermissionStatus.Denied(shouldShowRationale = true))

                else ->
                    onPermissionResult(PermissionStatus.Denied(shouldShowRationale = true))
            }
        }
    }

    private fun getCurrentNotificationCenter(): UNUserNotificationCenter {
        return UNUserNotificationCenter.currentNotificationCenter()
    }
}