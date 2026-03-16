package com.mohamedrejeb.calf.permissions

val Permission.Companion.Camera: Permission
    get() = PermissionCamera

internal expect fun registerCameraPermission(permission: Permission)

internal object PermissionCamera : Permission("Camera") {
    init {
        registerCameraPermission(this)
    }
}
