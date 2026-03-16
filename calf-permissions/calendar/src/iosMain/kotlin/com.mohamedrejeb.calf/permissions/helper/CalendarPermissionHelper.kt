package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusDenied
import platform.EventKit.EKAuthorizationStatusFullAccess
import platform.EventKit.EKAuthorizationStatusNotDetermined
import platform.EventKit.EKAuthorizationStatusRestricted
import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStore

internal class CalendarPermissionHelper : PermissionHelper {
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handleLaunchPermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                EKEventStore().requestFullAccessToEventsWithCompletion { isOk, error ->
                    onPermissionResult(isOk && error == null)
                }
            }
        )
    }

    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val status = EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
        val permissionStatus = when (status) {
            EKAuthorizationStatusAuthorized,
            EKAuthorizationStatusFullAccess ->
                PermissionStatus.Granted

            EKAuthorizationStatusNotDetermined ->
                PermissionStatus.Denied(shouldShowRationale = false)

            EKAuthorizationStatusDenied,
            EKAuthorizationStatusRestricted ->
                PermissionStatus.Denied(shouldShowRationale = true)

            else ->
                PermissionStatus.Denied(shouldShowRationale = true)
        }
        onPermissionResult(permissionStatus)
    }
}
