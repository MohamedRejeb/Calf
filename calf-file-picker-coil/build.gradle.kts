plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlinMultiplatformSetup()
modulePublicationSetup()

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfCore)
        api(projects.calfIo)
        implementation(libs.coil)
    }
}