import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
        }
    }

    sourceSets.jvmMain.dependencies {
        implementation(projects.sample.common)
        implementation(projects.calfFilePicker)
        implementation(compose.desktop.currentOs)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Calf"
            packageVersion = "1.0.0"

            macOS {
                jvmArgs(
                    "-Dapple.awt.application.appearance=system",
                )
            }
        }
    }
}
