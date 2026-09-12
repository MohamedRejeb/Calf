plugins {
    id("compose.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets.desktopTest.dependencies {
        implementation(libs.compose.ui.test)
        implementation(compose.desktop.currentOs)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.android.navigation.compose)
    }
}
