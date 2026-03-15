package com.mohamedrejeb.calf.permissions

val Permission.Companion.FineLocation: Permission
    get() = PermissionFineLocation

val Permission.Companion.CoarseLocation: Permission
    get() = PermissionCoarseLocation

val Permission.Companion.BackgroundLocation: Permission
    get() = PermissionBackgroundLocation

internal expect fun registerFineLocationPermission(permission: Permission)

internal expect fun registerCoarseLocationPermission(permission: Permission)

internal expect fun registerBackgroundLocationPermission(permission: Permission)

internal object PermissionFineLocation : Permission("FineLocation") {
    init {
        registerFineLocationPermission(this)
    }
}

internal object PermissionCoarseLocation : Permission("CoarseLocation") {
    init {
        registerCoarseLocationPermission(this)
    }
}

internal object PermissionBackgroundLocation : Permission("BackgroundLocation") {
    init {
        registerBackgroundLocationPermission(this)
    }
}
