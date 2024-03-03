import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    jvm("desktop") {
        jvmToolchain(11)
    }
    js {
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

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

        // Calf
        // This is possible thanks to `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`
        // in `settings.gradle.kts
        api(projects.calfUi)
        implementation(projects.calfFilePicker)
        implementation(projects.calfPermissions)
        implementation(projects.calfNavigation)
    }
}

android {
    namespace = "com.mohamedrejeb.calf.sample.common"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
