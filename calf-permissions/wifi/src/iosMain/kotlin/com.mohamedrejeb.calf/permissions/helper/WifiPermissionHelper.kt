package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.Foundation.NSNetService
import platform.Foundation.NSNetServiceDelegateProtocol
import platform.Foundation.NSTimer
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationState.UIApplicationStateActive
import platform.darwin.NSObject

internal class WifiPermissionHelper : PermissionHelper {
    private var currentPermissionDelegate: LocalNetworkPermissionDelegate? = null

    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handleLaunchPermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                currentPermissionDelegate?.cancel()

                val newDelegate = LocalNetworkPermissionDelegate { completedDelegate, granted ->
                    onPermissionResult(granted)

                    if (currentPermissionDelegate === completedDelegate) {
                        currentPermissionDelegate = null
                    }
                }
                currentPermissionDelegate = newDelegate
                newDelegate.startCheck()
            }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        currentPermissionDelegate?.cancel()

        val newDelegate = LocalNetworkPermissionDelegate { completedDelegate, granted ->
            val status = if (granted) {
                PermissionStatus.Granted
            } else {
                PermissionStatus.Denied(shouldShowRationale = true)
            }

            onPermissionResult(status)

            if (currentPermissionDelegate === completedDelegate) {
                currentPermissionDelegate = null
            }
        }
        currentPermissionDelegate = newDelegate
        newDelegate.startCheck()
    }
}

/**
 * Manages the NSNetService interaction to check/trigger local network permission.
 */
private class LocalNetworkPermissionDelegate(
    private val onComplete: (delegate: LocalNetworkPermissionDelegate, granted: Boolean) -> Unit
) : NSObject(), NSNetServiceDelegateProtocol {

    private var netService: NSNetService? = null
    private var timeoutTimer: NSTimer? = null
    private var isCompleted = false

    fun startCheck() {
        if (UIApplication.sharedApplication.applicationState != UIApplicationStateActive) {
            cleanupAndComplete(false)
            return
        }

        netService = NSNetService(
            domain = "local.",
            type = "_lnp._tcp.",
            name = "CalfWifiPermissionCheck",
            port = 0
        )
        if (netService == null) {
            cleanupAndComplete(false)
            return
        }

        netService?.delegate = this
        timeoutTimer = NSTimer.scheduledTimerWithTimeInterval(3.0, repeats = false) {
            cleanupAndComplete(false)
        }

        netService?.publish()
    }

    private fun cleanupAndComplete(granted: Boolean) {
        if (isCompleted) return

        isCompleted = true
        timeoutTimer?.invalidate()

        timeoutTimer = null
        netService?.stop()

        netService?.delegate = null
        netService = null

        onComplete(this, granted)
    }

    override fun netServiceDidPublish(sender: NSNetService) {
        cleanupAndComplete(true)
    }

    override fun netService(sender: NSNetService, didNotPublish: Map<Any?, *>) {
        cleanupAndComplete(false)
    }

    fun cancel() {
        cleanupAndComplete(false)
    }
}
