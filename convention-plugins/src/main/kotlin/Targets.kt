import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Declares the non-Android targets shared by every published module.
 *
 * The Android target is created by the `com.android.kotlin.multiplatform.library` plugin and
 * configured in [androidLibrarySetup].
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.applyTargets() {
    jvm("desktop")

    js().browser()

    // Deprecated: ExperimentalWasmDsl annotation class is removed in Kotlin 2.3
    // See: https://kotlinlang.org/docs/compatibility-guide-23.html#deprecate-experimentalwasmdsl-annotation-class
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs().browser()

    iosArm64()
    iosSimulatorArm64()
}

/**
 * Bundles the JS and Wasm test executables with webpack so browser tests can load the Skiko runtime.
 *
 * Compose Multiplatform 1.12+ refuses to run `jsBrowserTest`/`wasmJsBrowserTest` for a target without an
 * executable binary (https://youtrack.jetbrains.com/issue/CMP-4906). Call this from modules whose tests
 * run in the browser (e.g. `commonTest`). It also adds a webpack distribution of the module to `assemble`,
 * so it is deliberately not applied to every library; the published klibs are unaffected.
 */
fun KotlinMultiplatformExtension.withBrowserTestBundling() {
    js { binaries.executable() }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { binaries.executable() }
}
