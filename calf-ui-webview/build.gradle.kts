import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("module.publication")
}

val os: OperatingSystem = OperatingSystem.current()
val arch: String = System.getProperty("os.arch")
val isAarch64: Boolean = arch.contains("aarch64")

val platform = when {
    os.isWindows -> "win"
    os.isMacOsX -> "mac"
    else -> "linux"
} + if (isAarch64) "-aarch64" else ""

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
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets {
        val commonMain by getting

        val androidMain by getting {
            dependencies {
                implementation(libs.activity.compose)
                implementation(libs.kotlinx.coroutines.android)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation("org.openjfx:javafx-base:19:$platform")
                implementation("org.openjfx:javafx-graphics:19:$platform")
                implementation("org.openjfx:javafx-controls:19:$platform")
                implementation("org.openjfx:javafx-media:19:$platform")
                implementation("org.openjfx:javafx-web:19:$platform")
                implementation("org.openjfx:javafx-swing:19:$platform")
                implementation(libs.kotlinx.coroutines.javafx)
            }
        }
    }
}

android {
    namespace = "com.mohamedrejeb.calf.ui.webview"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
