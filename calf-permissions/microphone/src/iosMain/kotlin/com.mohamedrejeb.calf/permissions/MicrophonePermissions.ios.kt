package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.AVMicrophonePermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry
import platform.AVFoundation.AVMediaTypeAudio

internal actual fun registerRecordAudioPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        AVMicrophonePermissionHelper(AVMediaTypeAudio)
    }
}
