plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets.jsMain.dependencies {
        implementation(projects.sample.common)

        implementation(compose.ui)
        implementation(compose.html.core)
    }
}

compose.experimental {
    web.application {}
}
