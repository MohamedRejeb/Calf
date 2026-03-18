plugins {
    id("compose.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
    }
    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }
}
