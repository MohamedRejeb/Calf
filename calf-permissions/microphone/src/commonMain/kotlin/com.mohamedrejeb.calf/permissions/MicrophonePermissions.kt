package com.mohamedrejeb.calf.permissions

val Permission.Companion.RecordAudio: Permission
    get() = PermissionRecordAudio

internal expect fun registerRecordAudioPermission(permission: Permission)

internal object PermissionRecordAudio : Permission("RecordAudio") {
    init {
        registerRecordAudioPermission(this)
    }
}
