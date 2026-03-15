package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val cameraRegistration = run {
    PermissionDelegateRegistry.register(Permission.Camera) {
        AVCapturePermissionHelper(AVMediaTypeVideo)
    }
    PermissionDelegateRegistry.register(Permission.RecordAudio) {
        AVCapturePermissionHelper(AVMediaTypeAudio)
    }
}
