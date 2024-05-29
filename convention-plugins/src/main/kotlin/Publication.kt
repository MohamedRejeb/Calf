import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign

fun Project.rootPublicationSetup() {
    apply(plugin = "io.github.gradle-nexus.publish-plugin")

    allprojects {
        group = "com.mohamedrejeb.calf"
        version = "0.4.1"
    }

    nexusPublishing {
        // Configure maven central repository
        // https://github.com/gradle-nexus/publish-plugin#publishing-to-maven-central-via-sonatype-ossrh
        repositories {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
                stagingProfileId.set(System.getenv("OSSRH_STAGING_PROFILE_ID"))
                username.set(System.getenv("OSSRH_USERNAME"))
                password.set(System.getenv("OSSRH_PASSWORD"))
            }
        }
    }
}

fun Project.modulePublicationSetup() {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    publishing {
        // Configure all publications
        publications.withType<MavenPublication> {
            // Stub javadoc.jar artifact
            artifact(
                tasks.register("${name}JavadocJar", Jar::class) {
                    archiveClassifier.set("javadoc")
                    archiveAppendix.set(this@withType.name)
                },
            )

            // Provide artifacts information required by Maven Central
            pom {
                name.set("Calf - Compose Adaptive Look & Feel")
                description.set("Calf is a library that allows you to easily create adaptive UIs for your Compose Multiplatform apps.")
                url.set("https://github.com/MohamedRejeb/Calf")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("MohamedRejeb")
                        name.set("Mohamed Rejeb")
                        email.set("mohamedrejeb445@gmail.com")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/MohamedRejeb/Calf/issues")
                }
                scm {
                    connection.set("https://github.com/MohamedRejeb/Calf.git")
                    url.set("https://github.com/MohamedRejeb/Calf")
                }
            }
        }
    }

    signing {
        useInMemoryPgpKeys(
            System.getenv("OSSRH_GPG_SECRET_KEY_ID"),
            System.getenv("OSSRH_GPG_SECRET_KEY"),
            System.getenv("OSSRH_GPG_SECRET_KEY_PASSWORD"),
        )
        sign(publishing.publications)
    }

    // TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
    project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        dependsOn(project.tasks.withType(Sign::class.java))
    }
}
