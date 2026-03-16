package com.mohamedrejeb.calf.permissions

val Permission.Companion.WifiAccessState: Permission
    get() = PermissionWifiAccessState

val Permission.Companion.WifiChangeState: Permission
    get() = PermissionWifiChangeState

val Permission.Companion.WifiNearbyDevices: Permission
    get() = PermissionWifiNearbyDevices

internal expect fun registerWifiAccessStatePermission(permission: Permission)

internal expect fun registerWifiChangeStatePermission(permission: Permission)

internal expect fun registerWifiNearbyDevicesPermission(permission: Permission)

internal object PermissionWifiAccessState : Permission("WifiAccessState") {
    init {
        registerWifiAccessStatePermission(this)
    }
}

internal object PermissionWifiChangeState : Permission("WifiChangeState") {
    init {
        registerWifiChangeStatePermission(this)
    }
}

internal object PermissionWifiNearbyDevices : Permission("WifiNearbyDevices") {
    init {
        registerWifiNearbyDevicesPermission(this)
    }
}
