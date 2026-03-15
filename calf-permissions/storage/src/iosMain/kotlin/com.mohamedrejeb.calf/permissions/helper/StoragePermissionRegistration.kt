package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val storageRegistration = run {
    PermissionDelegateRegistry.register(Permission.ReadStorage) {
        GrantedPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.WriteStorage) {
        GrantedPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.ReadAudio) {
        GrantedPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.Call) {
        GrantedPermissionHelper()
    }
}
