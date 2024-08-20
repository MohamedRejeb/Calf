package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary

internal class GalleryPermissionHelper : PermissionHelper {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handlePermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                PHPhotoLibrary.requestAuthorization {
                    onPermissionResult(it == PHAuthorizationStatusAuthorized)
                }
            }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val status = getCurrentAuthorizationStatus()
        val permissionStatus = when (status) {
            PHAuthorizationStatusAuthorized ->
                PermissionStatus.Granted

            PHAuthorizationStatusNotDetermined ->
                PermissionStatus.Denied(shouldShowRationale = true)

            else ->
                PermissionStatus.Denied(shouldShowRationale = false)
        }
        onPermissionResult(permissionStatus)
    }

    private fun getCurrentAuthorizationStatus(): PHAuthorizationStatus {
        return PHPhotoLibrary.authorizationStatus()
    }
}