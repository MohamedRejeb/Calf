import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
}

composeMultiplatformSetup()
modulePublicationSetup()

val os: OperatingSystem = OperatingSystem.current()
val arch: String = System.getProperty("os.arch")
val isAarch64: Boolean = arch.contains("aarch64")

val platform =
    when {
        os.isWindows -> "win"
        os.isMacOsX -> "mac"
        else -> "linux"
    } + if (isAarch64) "-aarch64" else ""

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
        implementation(libs.kotlinx.coroutines.android)
    }

    sourceSets.desktopMain.dependencies {
        implementation("org.openjfx:javafx-base:19:$platform")
        implementation("org.openjfx:javafx-graphics:19:$platform")
        implementation("org.openjfx:javafx-controls:19:$platform")
        implementation("org.openjfx:javafx-media:19:$platform")
        implementation("org.openjfx:javafx-web:19:$platform")
        implementation("org.openjfx:javafx-swing:19:$platform")
        implementation(libs.kotlinx.coroutines.swing)
    }
}
