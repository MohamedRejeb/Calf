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
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "Calf"
include(
    ":calf-ui",
    ":calf-sf-symbols",
    ":calf-navigation",
    ":calf-file-picker",
    ":calf-io",

    ":sample:android",
    ":sample:desktop",
//    ":sample:web",
    ":sample:common",
)
