package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.AVCapturePermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo

internal actual fun registerCameraPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        AVCapturePermissionHelper(AVMediaTypeVideo)
    }
}

internal actual fun registerRecordAudioPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        AVCapturePermissionHelper(AVMediaTypeAudio)
    }
}
