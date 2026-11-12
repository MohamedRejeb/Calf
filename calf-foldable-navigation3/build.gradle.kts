plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    // commonTest runs in the browser, which needs the Skiko runtime bundled by webpack.
    withBrowserTestBundling()

    // The module implements these APIs, so it does not need to be told about them.
    sourceSets.all {
        languageSettings.optIn("com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi")
        languageSettings.optIn("androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi")
    }

    sourceSets.commonMain.dependencies {
        // Public: rememberCalfWindowAdaptiveInfo() and the opt-in marker come from here.
        api(projects.calfFoldable)
        // Public: the directive and the scene strategy are these libraries' types.
        api(libs.compose.material3.adaptive.navigation3)
        implementation(libs.compose.foundation)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }
}
