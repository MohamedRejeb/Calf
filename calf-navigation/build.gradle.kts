plugins {
    id("compose.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.navigation.compose)
    }
}
