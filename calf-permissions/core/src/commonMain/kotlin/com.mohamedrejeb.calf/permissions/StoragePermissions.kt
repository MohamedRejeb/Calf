package com.mohamedrejeb.calf.permissions

val Permission.Companion.ReadStorage: Permission
    get() = PermissionReadStorage

val Permission.Companion.WriteStorage: Permission
    get() = PermissionWriteStorage

val Permission.Companion.ReadAudio: Permission
    get() = PermissionReadAudio

val Permission.Companion.Call: Permission
    get() = PermissionCall

internal expect fun registerReadStoragePermission(permission: Permission)

internal expect fun registerWriteStoragePermission(permission: Permission)

internal expect fun registerReadAudioPermission(permission: Permission)

internal expect fun registerCallPermission(permission: Permission)

internal object PermissionReadStorage : Permission("ReadStorage") {
    init {
        registerReadStoragePermission(this)
    }
}

internal object PermissionWriteStorage : Permission("WriteStorage") {
    init {
        registerWriteStoragePermission(this)
    }
}

internal object PermissionReadAudio : Permission("ReadAudio") {
    init {
        registerReadAudioPermission(this)
    }
}

internal object PermissionCall : Permission("Call") {
    init {
        registerCallPermission(this)
    }
}
