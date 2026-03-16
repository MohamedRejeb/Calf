package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.GalleryPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionDelegateRegistry

internal actual fun registerGalleryPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        GalleryPermissionHelper()
    }
}

internal actual fun registerReadImagePermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        GalleryPermissionHelper()
    }
}

internal actual fun registerReadVideoPermission(permission: Permission) {
    PermissionDelegateRegistry.register(permission) {
        GalleryPermissionHelper()
    }
}
