package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.AVCameraPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry
import platform.AVFoundation.AVMediaTypeVideo

internal actual fun registerCameraPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        AVCameraPermissionHelper(AVMediaTypeVideo)
    }
}
