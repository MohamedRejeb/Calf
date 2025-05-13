plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    js {
        outputModuleName = "web"
        browser {
            commonWebpackConfig {
                outputFileName = "web.js"
            }
        }
        binaries.executable()
    }

    sourceSets.jsMain.dependencies {
        implementation(projects.sample.common)

        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.ui)
    }
}