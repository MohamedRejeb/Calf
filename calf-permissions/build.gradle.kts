plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfPermissions.calfPermissionsCore)
        api(projects.calfPermissions.calfPermissionsCamera)
        api(projects.calfPermissions.calfPermissionsGallery)
        api(projects.calfPermissions.calfPermissionsLocation)
        api(projects.calfPermissions.calfPermissionsBluetooth)
        api(projects.calfPermissions.calfPermissionsContacts)
        api(projects.calfPermissions.calfPermissionsCalendar)
        api(projects.calfPermissions.calfPermissionsNotifications)
        api(projects.calfPermissions.calfPermissionsWifi)
        api(projects.calfPermissions.calfPermissionsStorage)
        api(projects.calfPermissions.calfPermissionsMicrophone)
    }
}
