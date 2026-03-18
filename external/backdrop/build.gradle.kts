plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
        implementation(libs.compose.ui.graphics)
        implementation(projects.external.shapes)
        implementation("org.jetbrains:annotations:26.0.2-1")
    }
    sourceSets.all {
        languageSettings.enableLanguageFeature("ContextParameters")
    }
}
