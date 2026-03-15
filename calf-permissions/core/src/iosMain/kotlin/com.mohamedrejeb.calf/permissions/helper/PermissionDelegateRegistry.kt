package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission

object PermissionDelegateRegistry {
    private val delegates = mutableMapOf<Permission, () -> PermissionHelper>()

    fun register(permission: Permission, factory: () -> PermissionHelper) {
        delegates[permission] = factory
    }

    fun getDelegate(permission: Permission): PermissionHelper {
        return delegates[permission]?.invoke()
            ?: error(
                "No delegate registered for $permission. " +
                    "Did you add the corresponding calf-permissions-* dependency?"
            )
    }
}
