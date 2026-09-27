plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    android {
        // Packs the SF Symbols font (composeResources) into the AAR. The AGP 9 KMP library target
        // leaves Android resources off by default, and the font is then missing at runtime.
        androidResources.enable = true
    }

    sourceSets.commonMain.dependencies {
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.components.resources)
    }
}
