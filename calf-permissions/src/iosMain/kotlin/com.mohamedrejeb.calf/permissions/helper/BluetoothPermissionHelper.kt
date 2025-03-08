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
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject

internal class BluetoothPermissionHelper : PermissionHelper {
    @OptIn(ExperimentalForeignApi::class, ExperimentalPermissionsApi::class)
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handlePermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                CBCentralManager(object : NSObject(), CBCentralManagerDelegateProtocol {
                    override fun centralManagerDidUpdateState(central: CBCentralManager) {
                        onPermissionResult(central.state == CBManagerStatePoweredOn)
                    }
                }, null)
            }
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        if (CBManager.resolveClassMethod(NSSelectorFromString("authorization"))) {
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