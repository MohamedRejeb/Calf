package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus

internal class GrantedPermissionHelper: PermissionHelper {
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        onPermissionResult(true)
    }

    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) =
        onPermissionResult(PermissionStatus.Granted)
}