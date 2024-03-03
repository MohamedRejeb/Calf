import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Retrieves the [kotlin][org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension] extension.
 */
internal val org.gradle.api.Project.kotlin: org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName(
        "kotlin",
    ) as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures the [kotlin][org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension] extension.
 */
internal fun org.gradle.api.Project.kotlin(configure: Action<KotlinMultiplatformExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlin", configure)

/**
 * Retrieves the [compose][org.jetbrains.compose.ComposePlugin.Dependencies] extension.
 */
internal val org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension.compose: org.jetbrains.compose.ComposePlugin.Dependencies get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("compose") as org.jetbrains.compose.ComposePlugin.Dependencies

/**
 * Configures the [compose][org.jetbrains.compose.ComposePlugin.Dependencies] extension.
 */
internal fun org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension.compose(
    configure: Action<org.jetbrains.compose.ComposePlugin.Dependencies>,
): Unit = (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("compose", configure)

/**
 * Retrieves the [nexusPublishing][io.github.gradlenexus.publishplugin.NexusPublishExtension] extension.
 */
internal val org.gradle.api.Project.`nexusPublishing`: io.github.gradlenexus.publishplugin.NexusPublishExtension get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName(
        "nexusPublishing",
    ) as io.github.gradlenexus.publishplugin.NexusPublishExtension

/**
 * Configures the [nexusPublishing][io.github.gradlenexus.publishplugin.NexusPublishExtension] extension.
 */
internal fun org.gradle.api.Project.`nexusPublishing`(configure: Action<NexusPublishExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("nexusPublishing", configure)

/**
 * Retrieves the [publishing][org.gradle.api.publish.PublishingExtension] extension.
 */
internal val org.gradle.api.Project.`publishing`: org.gradle.api.publish.PublishingExtension get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("publishing") as org.gradle.api.publish.PublishingExtension

/**
 * Configures the [publishing][org.gradle.api.publish.PublishingExtension] extension.
 */
internal fun org.gradle.api.Project.`publishing`(configure: Action<org.gradle.api.publish.PublishingExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("publishing", configure)

/**
 * Retrieves the [signing][org.gradle.plugins.signing.SigningExtension] extension.
 */
internal val org.gradle.api.Project.`signing`: org.gradle.plugins.signing.SigningExtension get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("signing") as org.gradle.plugins.signing.SigningExtension

/**
 * Configures the [signing][org.gradle.plugins.signing.SigningExtension] extension.
 */
internal fun org.gradle.api.Project.`signing`(configure: Action<org.gradle.plugins.signing.SigningExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("signing", configure)
