package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.AVCapturePermissionDelegate
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegate
import platform.AVFoundation.AVMediaTypeVideo

internal fun Permission.getPermissionDelegate(): PermissionDelegate {
    return when (this) {
        Permission.Camera -> AVCapturePermissionDelegate(
            type = AVMediaTypeVideo,
        )
        Permission.Gallery -> TODO()
        Permission.ReadStorage -> TODO()
        Permission.WriteStorage -> TODO()
        Permission.FileLocation -> TODO()
        Permission.CoarseLocation -> TODO()
        Permission.RemoteNotification -> TODO()
        Permission.RecordAudio -> TODO()
        Permission.BluetoothLe -> TODO()
        Permission.BluetoothScan -> TODO()
        Permission.BluetoothConnect -> TODO()
        Permission.BluetoothAdvertise -> TODO()
    }
}