import java.io.File
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

/** Prefix of the validation tasks the Compose Multiplatform plugin wires before every `<target>BrowserTest`. */
private const val COMPOSE_UI_TEST_CHECK_TASK_PREFIX = "checkComposeUiTestConfigurationFor"

/**
 * Limits Compose Multiplatform's `checkComposeUiTestConfigurationFor<Target>` tasks to targets that have
 * test sources.
 *
 * Since CMP 1.12 the check fails whenever Skiko is on a web target's test classpath and the target has no
 * executable binary (https://youtrack.jetbrains.com/issue/CMP-4906). It runs ahead of `jsBrowserTest` and
 * `wasmJsBrowserTest` even when those tasks have nothing to run, which would force every library module
 * to be bundled as a web application. Modules that do have browser tests must call
 * [withBrowserTestBundling]; for the rest the check has nothing to protect and is skipped.
 */
fun Project.skipComposeUiTestCheckWithoutTestSources() {
    tasks.named { it.startsWith(COMPOSE_UI_TEST_CHECK_TASK_PREFIX) }.configureEach {
        val targetName = name
            .removePrefix(COMPOSE_UI_TEST_CHECK_TASK_PREFIX)
            .replaceFirstChar(Char::lowercase)
        val hasTestSources = hasTestSources(targetName)
        onlyIf("target '$targetName' has no test sources") { hasTestSources }
    }
}

private fun Project.hasTestSources(targetName: String): Boolean {
    val target = the<KotlinMultiplatformExtension>().targets.findByName(targetName) ?: return false
    val testCompilation = target.compilations.findByName(KotlinCompilation.TEST_COMPILATION_NAME) ?: return false
    return testCompilation.allKotlinSourceSets.any { sourceSet ->
        sourceSet.kotlin.sourceDirectories.any { dir -> dir.walkTopDown().any(File::isFile) }
    }
}
