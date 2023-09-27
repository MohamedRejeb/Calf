package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary

internal class GalleryPermissionHelper : PermissionHelper {
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        val status = getCurrentAuthorizationStatus()
        when(status) {
            AVAuthorizationStatusAuthorized -> onPermissionResult(true)
            else -> {
                PHPhotoLibrary.requestAuthorization {
                    when (it) {
                        PHAuthorizationStatusAuthorized -> onPermissionResult(true)
                        else -> onPermissionResult(false)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val status = getCurrentAuthorizationStatus()
        val permissionStatus = when(status) {
            PHAuthorizationStatusAuthorized -> PermissionStatus.Granted
            else -> PermissionStatus.Denied(false)
        }
        onPermissionResult(permissionStatus)
    }

    private fun getCurrentAuthorizationStatus(): PHAuthorizationStatus {
        return PHPhotoLibrary.authorizationStatus()
    }
}