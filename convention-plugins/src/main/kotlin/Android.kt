import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Name under which `com.android.kotlin.multiplatform.library` registers its target DSL
 * inside the `kotlin { }` extension, i.e. `kotlin { android { } }`.
 */
private const val ANDROID_LIBRARY_EXTENSION_NAME = "android"

/**
 * Configures the Android target declared by the `com.android.kotlin.multiplatform.library` plugin.
 *
 * AGP 9 no longer allows `com.android.library` and `org.jetbrains.kotlin.multiplatform` in the same
 * subproject, so the Android target comes from that plugin instead of KGP's `androidTarget()`.
 * The plugin builds a single variant and publishes it through the regular KMP publication.
 */
fun Project.androidLibrarySetup() {
    val libs = the<LibrariesForLibs>()
    val androidNamespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
    val jvmTarget = JvmTarget.fromTarget(libs.versions.java.get())

    extensions.configure<KotlinMultiplatformExtension> {
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>(
            ANDROID_LIBRARY_EXTENSION_NAME,
        ) {
            namespace = androidNamespace
            compileSdk = libs.versions.android.compileSdk.get().toInt()
            minSdk = libs.versions.android.minSdk.get().toInt()
            compilerOptions.jvmTarget.set(jvmTarget)
            // Runs commonTest on the Android JVM, like the old androidUnitTest source set did.
            withHostTest {}
        }
    }
}
