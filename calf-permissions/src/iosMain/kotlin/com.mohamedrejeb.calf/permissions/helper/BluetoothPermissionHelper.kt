package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManager
import platform.CoreBluetooth.CBManagerAuthorization
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.UIKit.UIDevice
import platform.darwin.NSObject

internal class BluetoothPermissionHelper : PermissionHelper {
    private val iOS13AndHigher: Boolean
        get() = UIDevice.currentDevice.systemVersion.toDoubleOrNull()?.let { it >= 13.0 } == true

    @OptIn(ExperimentalForeignApi::class, ExperimentalPermissionsApi::class)
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handleLaunchPermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                if (iOS13AndHigher) {
                    val initialAuth = CBManager.authorization

                    // If permission already determined, return immediately
                    if (initialAuth != CBManagerAuthorizationNotDetermined) {
                        onPermissionResult(initialAuth == CBManagerAuthorizationAllowedAlways)

                        return@handleLaunchPermissionRequest
                    }
                }

                var callbackFired = false

                // CBCentralManager needed to trigger permission dialog
                CBCentralManager(object : NSObject(), CBCentralManagerDelegateProtocol {
                    override fun centralManagerDidUpdateState(central: CBCentralManager) {
                        if (!callbackFired) {
                            callbackFired = true

                            if (iOS13AndHigher) {
                                onPermissionResult(CBManager.authorization == CBManagerAuthorizationAllowedAlways)
                            } else {
                                onPermissionResult(central.state == CBManagerStatePoweredOn)
                            }
                        }
                    }
                }, null)
            }
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        if (iOS13AndHigher) {
            val state: CBManagerAuthorization = CBManager.authorization
            when (state) {
                CBManagerAuthorizationAllowedAlways ->
                    onPermissionResult(PermissionStatus.Granted)

                CBManagerAuthorizationNotDetermined ->
                    onPermissionResult(PermissionStatus.Denied(shouldShowRationale = false))

                CBManagerAuthorizationDenied ->
                    onPermissionResult(PermissionStatus.Denied(shouldShowRationale = true))

                else ->
                    onPermissionResult(PermissionStatus.Denied(shouldShowRationale = true))
            }
        } else {
            val state = CBCentralManager().state
            when (state) {
                CBManagerStatePoweredOn ->
                    onPermissionResult(PermissionStatus.Granted)

                CBManagerStateUnknown ->
                    onPermissionResult(PermissionStatus.Denied(shouldShowRationale = false))

                else ->
                    onPermissionResult(PermissionStatus.Denied(shouldShowRationale = true))
            }
        }
    }
}
