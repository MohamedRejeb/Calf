package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.AVCapturePermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry
import platform.AVFoundation.AVMediaTypeAudio

internal actual fun registerRecordAudioPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        AVCapturePermissionHelper(AVMediaTypeAudio)
    }
}
