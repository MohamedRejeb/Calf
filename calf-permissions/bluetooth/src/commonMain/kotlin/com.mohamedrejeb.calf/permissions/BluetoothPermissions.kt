package com.mohamedrejeb.calf.permissions

val Permission.Companion.BluetoothLe: Permission
    get() = PermissionBluetoothLe

val Permission.Companion.BluetoothScan: Permission
    get() = PermissionBluetoothScan

val Permission.Companion.BluetoothConnect: Permission
    get() = PermissionBluetoothConnect

val Permission.Companion.BluetoothAdvertise: Permission
    get() = PermissionBluetoothAdvertise

internal expect fun registerBluetoothLePermission(permission: Permission)

internal expect fun registerBluetoothScanPermission(permission: Permission)

internal expect fun registerBluetoothConnectPermission(permission: Permission)

internal expect fun registerBluetoothAdvertisePermission(permission: Permission)

internal object PermissionBluetoothLe : Permission("BluetoothLe") {
    init {
        registerBluetoothLePermission(this)
    }
}

internal object PermissionBluetoothScan : Permission("BluetoothScan") {
    init {
        registerBluetoothScanPermission(this)
    }
}

internal object PermissionBluetoothConnect : Permission("BluetoothConnect") {
    init {
        registerBluetoothConnectPermission(this)
    }
}

internal object PermissionBluetoothAdvertise : Permission("BluetoothAdvertise") {
    init {
        registerBluetoothAdvertisePermission(this)
    }
}
