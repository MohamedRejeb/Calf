package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val contactRegistration = run {
    PermissionDelegateRegistry.register(Permission.ReadContacts) {
        ContactPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.WriteContacts) {
        ContactPermissionHelper()
    }
}
