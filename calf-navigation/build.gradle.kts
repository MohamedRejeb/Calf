plugins {
    id("compose.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.androidx.navigation3.runtime)
        api(libs.androidx.navigation3.ui)
        api(libs.jetbrains.lifecycle.viewmodel.compose)
        api(libs.jetbrains.lifecycle.viewmodel.navigation3)
        api(projects.calfUi)
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
}
