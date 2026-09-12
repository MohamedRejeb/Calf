plugins {
    id("kotlin.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfCore)
        api(libs.kotlinx.io.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets.desktopTest.dependencies {
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.documentfile)
    }

    sourceSets.webMain.dependencies {
        implementation(libs.kotlinx.browser)
    }
}
