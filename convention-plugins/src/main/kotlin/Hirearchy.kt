import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.applyHierarchyTemplate() {
    applyDefaultHierarchyTemplate {
        common {
            group("material") {
                withAndroidLibraryTarget()
                withJvm()
                withJs()
                withWasmJs()
            }

            group("nonAndroid") {
                withJvm()
                withIos()
                withJs()
                withWasmJs()
            }

            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }
}

/**
 * Adds the Android target created by `com.android.kotlin.multiplatform.library` to a group.
 *
 * KGP's [KotlinHierarchyBuilder.withAndroidTarget] is an `instanceof KotlinAndroidTarget` check that
 * only matches KGP's own `androidTarget()`, so AGP's target has to be matched by its own type.
 * Without this, `materialMain` silently drops out of the Android compilation and every `expect`
 * with a `materialMain` actual fails with "has no 'actual' declaration".
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
private fun KotlinHierarchyBuilder.withAndroidLibraryTarget() {
    withCompilations { it.target is KotlinMultiplatformAndroidLibraryTarget }
}

val NamedDomainObjectContainer<KotlinSourceSet>.desktopMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("desktopMain")

val NamedDomainObjectContainer<KotlinSourceSet>.materialMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("materialMain")

val NamedDomainObjectContainer<KotlinSourceSet>.nonAndroidMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("nonAndroidMain")

val NamedDomainObjectContainer<KotlinSourceSet>.jsWasmMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("jsWasmMain")
