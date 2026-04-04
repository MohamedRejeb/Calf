import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

fun Project.androidLibrarySetup() {
    val libs = the<LibrariesForLibs>()

    extensions.configure<LibraryExtension> {
        namespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }
        compileOptions {
            val javaVersion = JavaVersion.toVersion(libs.versions.java.get().toInt())
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }
}
