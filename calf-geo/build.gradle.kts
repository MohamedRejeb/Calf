plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
}

composeMultiplatformSetup()

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
    }
    sourceSets.androidMain.dependencies {
        implementation(libs.appcompat)
        implementation(libs.lifecycle.extensions)
        implementation(libs.play.services.location)
    }
}
