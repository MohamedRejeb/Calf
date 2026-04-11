plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.calfCore)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
        implementation(libs.kotlinx.coroutines.android)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.kotlinx.coroutines.swing)
    }

    sourceSets.iosMain.dependencies {
        implementation(libs.kyant.backdrop)
        implementation(libs.kyant.shapes)
    }
}
