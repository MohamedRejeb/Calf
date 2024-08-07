plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
}

composeMultiplatformSetup()

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
        implementation(projects.calfPermissions)
        implementation(projects.calfWebview)
        implementation(projects.calfNavigation)
    }
}
