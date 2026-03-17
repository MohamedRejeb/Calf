plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.calfCore)
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha05")
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
        implementation(libs.kotlinx.coroutines.android)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.kotlinx.coroutines.swing)
    }

    sourceSets.iosArm64Main.dependencies {
        implementation("io.github.kyant0:shapes:1.2.0")
        implementation("io.github.kyant0:backdrop:2.0.0-alpha03")
    }

    sourceSets.iosSimulatorArm64Main.dependencies {
        implementation("io.github.kyant0:shapes:1.2.0")
        implementation("io.github.kyant0:backdrop:2.0.0-alpha03")
    }
}
