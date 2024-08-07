plugins {
    id("kotlin.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfCore)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.documentfile)
    }
}
