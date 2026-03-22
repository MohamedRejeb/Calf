plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), project.name, version.toString())

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
