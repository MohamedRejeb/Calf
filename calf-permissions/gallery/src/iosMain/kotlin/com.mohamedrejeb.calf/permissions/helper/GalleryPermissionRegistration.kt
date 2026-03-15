package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.Permission
import kotlin.native.EagerInitialization

@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val galleryRegistration = run {
    PermissionDelegateRegistry.register(Permission.Gallery) {
        GalleryPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.ReadImage) {
        GalleryPermissionHelper()
    }
    PermissionDelegateRegistry.register(Permission.ReadVideo) {
        GalleryPermissionHelper()
    }
}
