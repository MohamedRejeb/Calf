package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManager
import platform.CoreBluetooth.CBManagerAuthorization
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerAuthorizationRestricted
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject

internal class BluetoothPermissionHelper: PermissionHelper {
    @OptIn(ExperimentalForeignApi::class)
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        val isNotDetermined =
            if (CBManager.resolveClassMethod(NSSelectorFromString("authorization"))) {
                CBManager.authorization == CBManagerAuthorizationNotDetermined
            } else {
                CBCentralManager().state == CBManagerStateUnknown
            }

        if (isNotDetermined) {
            CBCentralManager(object : NSObject(), CBCentralManagerDelegateProtocol {
                override fun centralManagerDidUpdateState(central: CBCentralManager) {
                    handleBluetoothState(
                        state = central.state,
                        onPermissionResult = onPermissionResult
                    )
                }
            }, null)
        } else {
            handleBluetoothState(
                state = CBCentralManager().state,
                onPermissionResult = onPermissionResult
            )
        }
    }

    private fun handleBluetoothState(
        state: CBManagerState,
        onPermissionResult: (Boolean) -> Unit
    ) {
        when (state) {
            CBManagerStatePoweredOn -> onPermissionResult(true)
            else -> onPermissionResult(false)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    @ExperimentalPermissionsApi
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        if (CBManager.resolveClassMethod(NSSelectorFromString("authorization"))) {
            val state: CBManagerAuthorization = CBManager.authorization
            when (state) {
                CBManagerAuthorizationAllowedAlways,
                CBManagerAuthorizationRestricted -> onPermissionResult(PermissionStatus.Granted)
                else -> onPermissionResult(PermissionStatus.Denied(false))
            }
        } else {
            val state: CBManagerState = CBCentralManager().state
            when (state) {
                CBManagerStatePoweredOn -> onPermissionResult(PermissionStatus.Granted)
                else -> onPermissionResult(PermissionStatus.Denied(false))
            }
        }
    }
}