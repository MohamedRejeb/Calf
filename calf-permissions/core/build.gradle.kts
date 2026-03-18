plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.compose.foundation)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
    }
}
