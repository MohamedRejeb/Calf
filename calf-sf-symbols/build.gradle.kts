plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
//    id("module.publication")
}

kotlin {
    kotlin.applyDefaultHierarchyTemplate()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    jvm("desktop") {
        jvmToolchain(11)
    }
    js(IR) {
        browser()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets.all {
        languageSettings {
            optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
        }
    }

    sourceSets.commonMain.get().dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.components.resources)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(libs.kotlin.test)
    }
}

android {
    namespace = "com.mohamedrejeb.calf.sf.symbols"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
