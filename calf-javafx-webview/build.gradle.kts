import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
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
    jvm("desktop") {
        jvmToolchain(11)
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.openjfx:javafx-base:19:$platform")
                implementation("org.openjfx:javafx-graphics:19:$platform")
                implementation("org.openjfx:javafx-controls:19:$platform")
                implementation("org.openjfx:javafx-web:19:$platform")
                implementation("org.openjfx:javafx-swing:19:$platform")
            }
        }
    }
}