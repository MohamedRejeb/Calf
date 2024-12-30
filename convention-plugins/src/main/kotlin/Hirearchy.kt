import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.applyHierarchyTemplate() {
    applyDefaultHierarchyTemplate {
        common {
            group("material") {
                withAndroidTarget()
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

val NamedDomainObjectContainer<KotlinSourceSet>.desktopMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("desktopMain")

val NamedDomainObjectContainer<KotlinSourceSet>.materialMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("materialMain")

val NamedDomainObjectContainer<KotlinSourceSet>.nonAndroidMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("nonAndroidMain")

val NamedDomainObjectContainer<KotlinSourceSet>.jsWasmMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named("jsWasmMain")