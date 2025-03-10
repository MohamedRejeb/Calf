package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorized
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject

internal class LocationPermissionHelper : PermissionHelper {
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    private val manager = CLLocationManager()
    private val managerDelegate = LocationManagerDelegate {
        onPermissionResult?.invoke(it)
        onPermissionResult = null
    }

    init {
        manager.delegate = managerDelegate
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handlePermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                this.onPermissionResult = onPermissionResult
                manager.requestWhenInUseAuthorization()
            }
        )
    }

    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val status = getCurrentAuthorizationStatus()
        val permissionStatus = when (status) {
            kCLAuthorizationStatusAuthorized,
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse,
                ->
                PermissionStatus.Granted

            kCLAuthorizationStatusNotDetermined ->
                PermissionStatus.Denied(shouldShowRationale = true)

            else ->
                PermissionStatus.Denied(shouldShowRationale = false)
        }
        onPermissionResult(permissionStatus)
    }

    private fun getCurrentAuthorizationStatus(): CLAuthorizationStatus =
        CLLocationManager.authorizationStatus()
}

private class LocationManagerDelegate(
    private val onPermissionResult: (Boolean) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManager(
        manager: CLLocationManager,
        didChangeAuthorizationStatus: CLAuthorizationStatus,
    ) {
        when (didChangeAuthorizationStatus) {
            kCLAuthorizationStatusAuthorized,
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse,
                ->
                onPermissionResult(true)

            else ->
                onPermissionResult(false)
        }
    }
}