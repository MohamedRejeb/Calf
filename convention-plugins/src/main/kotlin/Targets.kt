import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.applyTargets() {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    jvmToolchain(11)
    jvm("desktop")

    js().browser()

    // Deprecated: ExperimentalWasmDsl annotation class is removed in Kotlin 2.3
    // See: https://kotlinlang.org/docs/compatibility-guide-23.html#deprecate-experimentalwasmdsl-annotation-class
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs().browser()

    iosX64()
    iosArm64()
    iosSimulatorArm64()
}
