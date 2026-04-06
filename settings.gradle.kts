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
    ":calf-cupertino-icons",
    ":calf-navigation",
    ":calf-io",
    ":calf-file-picker",
    ":calf-file-picker-coil",
    ":calf-permissions",
    ":calf-permissions:core",
    ":calf-permissions:camera",
    ":calf-permissions:gallery",
    ":calf-permissions:location",
    ":calf-permissions:background-location",
    ":calf-permissions:bluetooth",
    ":calf-permissions:contacts",
    ":calf-permissions:calendar",
    ":calf-permissions:notifications",
    ":calf-permissions:wifi",
    ":calf-permissions:microphone",
    ":calf-geo",
    ":calf-maps",
    ":calf-camera-picker",
    ":calf-share",
    ":external:backdrop",
    ":external:shapes",
    // Sample modules
    ":sample:android",
    ":sample:desktop",
    ":sample:web-js",
    ":sample:web-wasm",
    ":sample:common",
)

// Rename projects to keep published artifact names unchanged
project(":calf-permissions:core").name = "calf-permissions-core"
project(":calf-permissions:camera").name = "calf-permissions-camera"
project(":calf-permissions:gallery").name = "calf-permissions-gallery"
project(":calf-permissions:location").name = "calf-permissions-location"
project(":calf-permissions:background-location").name = "calf-permissions-background-location"
project(":calf-permissions:bluetooth").name = "calf-permissions-bluetooth"
project(":calf-permissions:contacts").name = "calf-permissions-contacts"
project(":calf-permissions:calendar").name = "calf-permissions-calendar"
project(":calf-permissions:notifications").name = "calf-permissions-notifications"
project(":calf-permissions:wifi").name = "calf-permissions-wifi"
project(":calf-permissions:microphone").name = "calf-permissions-microphone"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
