import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.setJvmTarget() {
    val libs = the<LibrariesForLibs>()

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
    }
}