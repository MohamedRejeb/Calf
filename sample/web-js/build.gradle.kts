plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets.jsMain.dependencies {
        implementation(projects.sample.common)

        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.ui)
    }
}

compose.experimental {
    web.application {}
}
