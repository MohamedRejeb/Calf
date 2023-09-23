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
    macosX64()
    macosArm64()

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

        val iosMain by getting
        val macosX64Main by getting
        val macosArm64Main by getting

        // Group for Material3 targets
        val materialMain by creating
        androidMain.dependsOn(materialMain)
        desktopMain.dependsOn(materialMain)
        materialMain.dependsOn(commonMain)

        // Group for UIKit targets
        val uiKitMain by creating
        iosMain.dependsOn(uiKitMain)
        macosX64Main.dependsOn(uiKitMain)
        macosArm64Main.dependsOn(uiKitMain)
        uiKitMain.dependsOn(commonMain)
    }
}

android {
    namespace = "com.mohamedrejeb.calf.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
