package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus

internal interface PermissionHelper {
    fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit)

    @OptIn(ExperimentalPermissionsApi::class)
    fun getPermissionStatus(
        onPermissionResult: (PermissionStatus) -> Unit
    )
}

@OptIn(ExperimentalPermissionsApi::class)
internal fun PermissionHelper.handlePermissionRequest(
    onPermissionResult: (Boolean) -> Unit,
    launchPermissionRequest: () -> Unit,
) {
    getPermissionStatus { status ->
        when (status) {
            is PermissionStatus.Granted ->
                onPermissionResult(true)

            is PermissionStatus.Denied ->
                if (status.shouldShowRationale)
                    onPermissionResult(false)
                else
                    launchPermissionRequest()
        }
    }
}