plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfIo)

        implementation(compose.runtime)
        implementation(compose.foundation)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
    }

}
