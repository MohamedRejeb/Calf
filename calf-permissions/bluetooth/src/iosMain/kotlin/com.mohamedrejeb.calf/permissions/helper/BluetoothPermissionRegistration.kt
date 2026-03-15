package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val bluetoothRegistration = run {
    PermissionDelegateRegistry.register(Permission.BluetoothLe) {
        BluetoothPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.BluetoothScan) {
        BluetoothPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.BluetoothConnect) {
        BluetoothPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.BluetoothAdvertise) {
        BluetoothPermissionHelper()
    }
}
