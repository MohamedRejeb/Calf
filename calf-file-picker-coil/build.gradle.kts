plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

modulePublicationSetup()
androidLibrarySetup()

kotlin {
    applyHierarchyTemplate()
    applyTargets()
    sourceSets.commonMain.dependencies {
        api(projects.calfCore)
        api(projects.calfIo)
        implementation(libs.coil)
    }
}