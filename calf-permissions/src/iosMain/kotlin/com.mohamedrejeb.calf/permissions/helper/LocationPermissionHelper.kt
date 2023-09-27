package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject

internal class LocationPermissionHelper: PermissionHelper {
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    private val manager = CLLocationManager()
    private val managerDelegate = LocationManagerDelegate {
        onPermissionResult?.invoke(it)
        onPermissionResult = null
    }

    init {
        manager.delegate = managerDelegate
    }

    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        val status = getCurrentAuthorizationStatus()

        when(status) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> onPermissionResult(true)

            kCLAuthorizationStatusNotDetermined -> {
                this.onPermissionResult = onPermissionResult
                manager.requestWhenInUseAuthorization()
            }

            else -> onPermissionResult(false)
        }
    }

    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val status = getCurrentAuthorizationStatus()
        val permissionStatus =  when(status) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> PermissionStatus.Granted

            else -> PermissionStatus.Denied(false)
        }
        onPermissionResult(permissionStatus)
    }

    private fun getCurrentAuthorizationStatus(): CLAuthorizationStatus {
        return CLLocationManager.authorizationStatus()
    }
}

private class LocationManagerDelegate(
    private val onPermissionResult: (Boolean) -> Unit
): NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManager(
        manager: CLLocationManager,
        didChangeAuthorizationStatus: CLAuthorizationStatus
    ) {
        when(didChangeAuthorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> onPermissionResult(true)

            else -> onPermissionResult(false)
        }
    }
}