plugins {
    id("compose.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.android.navigation.compose)
    }
}
