package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManager
import platform.CoreBluetooth.CBManagerAuthorization
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.Foundation.NSProcessInfo
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

@OptIn(ExperimentalForeignApi::class, ExperimentalPermissionsApi::class)
internal class BluetoothPermissionHelper : PermissionHelper {
    private var centralManager: CBCentralManager? = null

    private val iOS13AndHigher: Boolean
        get() = NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(
            cValue {
                majorVersion = 13L
                minorVersion = 0L
                patchVersion = 0L
            }
        )

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

                fun resolveAndCleanup(allowed: Boolean, central: CBCentralManager) {
                    if (callbackFired) return
                    callbackFired = true

                    onPermissionResult(allowed)

                    central.stopScan()
                    centralManager = null
                }

                // create and retain CBCentralManager needed to trigger permission dialog
                centralManager = CBCentralManager(object : NSObject(), CBCentralManagerDelegateProtocol {
                    override fun centralManagerDidUpdateState(central: CBCentralManager) {
                        if (callbackFired) return

                        // Wait until CoreBluetooth finishes initializing
                        if (central.state == CBManagerStateUnknown) return

                        if (iOS13AndHigher) {
                            val auth = CBManager.authorization

                            // Don't resolve if authorization is in NotDetermined state
                            if (auth == CBManagerAuthorizationNotDetermined) {
                                central.scanForPeripheralsWithServices(null, null)

                                val delayNs = 2_000_000_000L // 2s
                                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delayNs), dispatch_get_main_queue()) {
                                    val newAuth = CBManager.authorization
                                    if (newAuth != CBManagerAuthorizationNotDetermined) {
                                        resolveAndCleanup(newAuth == CBManagerAuthorizationAllowedAlways, central)
                                    }
                                }
                                return
                            } else {
                                resolveAndCleanup(auth == CBManagerAuthorizationAllowedAlways, central)
                                return
                            }
                        } else {
                            resolveAndCleanup(central.state == CBManagerStatePoweredOn, central)
                        }
                    }
                }, null)
            }
        )
    }

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
