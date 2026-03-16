package com.mohamedrejeb.calf.permissions

val Permission.Companion.BackgroundLocation: Permission
    get() = PermissionBackgroundLocation

internal expect fun registerBackgroundLocationPermission(permission: Permission)

internal object PermissionBackgroundLocation : Permission("BackgroundLocation") {
    init {
        registerBackgroundLocationPermission(this)
    }
}
