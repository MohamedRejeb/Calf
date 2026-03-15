package com.mohamedrejeb.calf.permissions

val Permission.Companion.Camera: Permission
    get() = PermissionCamera

val Permission.Companion.RecordAudio: Permission
    get() = PermissionRecordAudio

internal expect fun registerCameraPermission(permission: Permission)

internal expect fun registerRecordAudioPermission(permission: Permission)

internal object PermissionCamera : Permission("Camera") {
    init {
        registerCameraPermission(this)
    }
}

internal object PermissionRecordAudio : Permission("RecordAudio") {
    init {
        registerRecordAudioPermission(this)
    }
}
