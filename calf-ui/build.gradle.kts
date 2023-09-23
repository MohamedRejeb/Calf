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

    sourceSets.commonMain.get().dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets {
        val commonMain by getting

        val androidMain by getting {
            dependencies {
                implementation(libs.activity.compose)
//                implementation("androidx.compose.material3:material3:1.1.1")
            }
        }

        val desktopMain by getting

        // Group for Material3 targets
        val materialMain by creating
        androidMain.dependsOn(materialMain)
        desktopMain.dependsOn(materialMain)
        materialMain.dependsOn(commonMain)
    }
}

android {
    namespace = "com.mohamedrejeb.calf.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
