import org.gradle.api.Project

fun Project.composeMultiplatformSetup() {
    require(plugins.any { it.toString().startsWith("org.jetbrains.kotlin") }) {
        "Kotlin plugin must be applied before to use Compose Multiplatform"
    }
    require(plugins.any { it.toString().startsWith("org.jetbrains.compose") }) {
        "Compose plugin must be applied before to use Compose Multiplatform"
    }

    kotlin {
        applyHierarchyTemplate()

        applyTargets()
    }

    androidLibrarySetup()
}
