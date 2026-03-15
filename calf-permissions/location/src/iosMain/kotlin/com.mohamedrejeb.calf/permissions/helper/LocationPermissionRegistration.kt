package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val locationRegistration = run {
    PermissionDelegateRegistry.register(Permission.FineLocation) {
        LocationPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.CoarseLocation) {
        LocationPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.BackgroundLocation) {
        LocationPermissionHelper()
    }
}
