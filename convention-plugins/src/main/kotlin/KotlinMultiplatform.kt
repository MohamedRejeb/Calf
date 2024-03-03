import org.gradle.api.Project

fun Project.kotlinMultiplatformSetup() {
    require(plugins.any { it.toString().startsWith("org.jetbrains.kotlin") }) {
        "Kotlin plugin must be applied before to use Compose Multiplatform"
    }

    kotlin {
        applyHierarchyTemplate()

        applyTargets()
    }

    androidLibrarySetup()
}
