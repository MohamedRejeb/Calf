package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaType
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

internal class AVCapturePermissionHelper(
    private val type: AVMediaType,
) : PermissionHelper {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handlePermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                AVCaptureDevice.requestAccessForMediaType(type) {
                    onPermissionResult(it)
                }
            }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val status = getCurrentAuthorizationStatus()
        val permissionStatus = when (status) {
            AVAuthorizationStatusAuthorized ->
                PermissionStatus.Granted

            AVAuthorizationStatusNotDetermined ->
                PermissionStatus.Denied(shouldShowRationale = true)

            else ->
                PermissionStatus.Denied(shouldShowRationale = false)
        }

        onPermissionResult(permissionStatus)
    }

    private fun getCurrentAuthorizationStatus(): AVAuthorizationStatus {
        return AVCaptureDevice.authorizationStatusForMediaType(type)
    }
}