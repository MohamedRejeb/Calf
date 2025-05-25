package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.EventKit.EKEventStore

internal class CalendarPermissionHelper : PermissionHelper {
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handleLaunchPermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                EKEventStore().requestFullAccessToEventsWithCompletion { isOk, error ->
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
        onPermissionResult(PermissionStatus.Granted)
    }
}