package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaType
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

internal class AVCapturePermissionDelegate(
    private val type: AVMediaType,
): PermissionDelegate {
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        val status = currentAuthorizationStatus()
        when(status) {
            AVAuthorizationStatusAuthorized -> onPermissionResult(true)
            else -> {
                AVCaptureDevice.requestAccessForMediaType(type) {
                    onPermissionResult(it)
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun getPermissionStatus(): PermissionStatus {
        val status = currentAuthorizationStatus()
        return when(status) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.Granted
            else -> PermissionStatus.Denied(false)
        }
    }

    private fun currentAuthorizationStatus(): AVAuthorizationStatus {
        return AVCaptureDevice.authorizationStatusForMediaType(type)
    }
}