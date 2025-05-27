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
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
    "calf-camera-picker",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
