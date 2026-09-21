import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    // commonTest runs in the browser, which needs the Skiko runtime bundled by webpack.
    withBrowserTestBundling()

    // The iOS 27.1 hinge and reserved-region APIs are missing from Kotlin/Native's platform
    // libraries. The definition file reaches them at runtime and declares no SDK 27.1 symbol, so it
    // builds with Xcode 26 as well.
    targets.withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            compilations.getByName("main") {
                cinterops.create("foldable") {
                    definitionFile = file("src/iosMain/cinterop/foldable.def")
                }
            }
        }

    // The module implements these APIs, so it does not need to be told about them.
    sourceSets.all {
        languageSettings.optIn("com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi")
        languageSettings.optIn("com.mohamedrejeb.calf.core.CalfIosOnlyApi")
    }

    sourceSets.commonMain.dependencies {
        api(projects.calfCore)
        // Public because FoldState.toWindowPosture() returns its Posture.
        api(libs.compose.material3.adaptive)
        implementation(libs.compose.foundation)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.window)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
    }
}
