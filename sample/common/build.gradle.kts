plugins {
    id("compose.multiplatform")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Common"
            isStatic = true

            // IMPORTANT: Exporting calf-ui is required for some functionalities to work
            export(projects.calfUi)
        }
    }

    sourceSets.commonMain.dependencies {
        api(compose.runtime)
        api(compose.foundation)
        api(compose.material)
        api(compose.material3)
        implementation(compose.materialIconsExtended)

        implementation(libs.kotlinx.datetime)

        // Calf
        api(projects.calfUi)
        implementation(projects.calfFilePicker)
        implementation(projects.calfFilePickerCoil)
        implementation(projects.calfPermissions)
        implementation(projects.calfWebview)
        implementation(libs.compose.navigation)
        implementation(projects.calfNavigation)
        implementation(projects.calfCameraPicker)

        // Coil
        implementation(libs.coil.compose)
        implementation(libs.coil.network.ktor)

        // Ktor
        implementation(libs.ktor.client.core)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.ktor.client.okhttp)
    }

    sourceSets.desktopMain.dependencies {
        implementation(compose.desktop.currentOs)

        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.ktor.client.okhttp)
    }

    sourceSets.iosMain.dependencies {
        implementation(libs.ktor.client.darwin)
    }

    sourceSets.jsMain.dependencies {
        implementation(libs.ktor.client.js)
    }

    sourceSets.wasmJsMain.dependencies {
        implementation(libs.ktor.client.wasm)
    }
}
