plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
}

composeMultiplatformSetup()
modulePublicationSetup()

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
    }
}
