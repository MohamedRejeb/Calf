rootProject.name = "Calf"

pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        google {
            mavenContent {
                releasesOnly()
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

include(
    // Artifact modules
    ":calf-core",
    ":calf-ui",
    ":calf-webview",
    ":calf-sf-symbols",
    ":calf-navigation",
    ":calf-io",
    ":calf-file-picker",
    ":calf-file-picker-coil",
    ":calf-permissions",
    ":calf-geo",
    ":calf-maps",
    // Sample modules
    ":sample:android",
    ":sample:desktop",
    ":sample:web-js",
    ":sample:web-wasm",
    ":sample:common",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
