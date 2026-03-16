package com.mohamedrejeb.calf.permissions

val Permission.Companion.FineLocation: Permission
    get() = PermissionFineLocation

val Permission.Companion.CoarseLocation: Permission
    get() = PermissionCoarseLocation

internal expect fun registerFineLocationPermission(permission: Permission)

internal expect fun registerCoarseLocationPermission(permission: Permission)

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

