plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    // commonTest runs in the browser, which needs the Skiko runtime bundled by webpack.
    withBrowserTestBundling()

    // calf-navigation reuses calf-ui's iOS bridge helpers, which are @InternalCalfApi.
    sourceSets.all {
        languageSettings.optIn("com.mohamedrejeb.calf.core.InternalCalfApi")
    }

    sourceSets.commonMain.dependencies {
        api(libs.androidx.navigation3.runtime)
        api(libs.androidx.navigation3.ui)
        api(libs.jetbrains.lifecycle.viewmodel.compose)
        api(libs.jetbrains.lifecycle.viewmodel.navigation3)
        api(libs.jetbrains.savedstate)
        api(libs.compose.material3.adaptive.navigation.suite)
        api(projects.calfUi)
        // api: @CalfIosOnlyApi is part of the public list-detail API.
        api(projects.calfCore)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.ui.backhandler)
    }

    // Material 3's list-detail scene for Navigation 3. iOS uses a UISplitViewController instead.
    sourceSets.named("materialMain") {
        dependencies {
            implementation(libs.compose.material3.adaptive.navigation3)
        }
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets.desktopTest.dependencies {
        implementation(libs.compose.ui.test)
        implementation(compose.desktop.currentOs)
    }
}
