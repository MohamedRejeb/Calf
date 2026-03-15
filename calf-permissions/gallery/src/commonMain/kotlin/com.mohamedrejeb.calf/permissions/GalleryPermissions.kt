package com.mohamedrejeb.calf.permissions

val Permission.Companion.Gallery: Permission
    get() = PermissionGallery

val Permission.Companion.ReadImage: Permission
    get() = PermissionReadImage

val Permission.Companion.ReadVideo: Permission
    get() = PermissionReadVideo

internal expect fun registerGalleryPermission(permission: Permission)

internal expect fun registerReadImagePermission(permission: Permission)

internal expect fun registerReadVideoPermission(permission: Permission)

internal object PermissionGallery : Permission("Gallery") {
    init {
        registerGalleryPermission(this)
    }
}

internal object PermissionReadImage : Permission("ReadImage") {
    init {
        registerReadImagePermission(this)
    }
}

internal object PermissionReadVideo : Permission("ReadVideo") {
    init {
        registerReadVideoPermission(this)
    }
}
