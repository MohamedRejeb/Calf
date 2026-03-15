package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val wifiRegistration = run {
    PermissionDelegateRegistry.register(Permission.WifiAccessState) {
        WifiPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.WifiChangeState) {
        WifiPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.WifiNearbyDevices) {
        WifiPermissionHelper()
    }
}
