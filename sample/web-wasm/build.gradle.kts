import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "web"
        browser {
            commonWebpackConfig {
                outputFileName = "web.js"
            }
        }
        binaries.executable()
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.sample.common)

        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.ui)
    }
}
