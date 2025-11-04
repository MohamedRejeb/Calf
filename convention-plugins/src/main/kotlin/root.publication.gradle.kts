plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

allprojects {
    group = "com.mohamedrejeb.calf"
    version = System.getenv("VERSION") ?: "0.9.0"
}

nexusPublishing {
    repositories {
        sonatype()
    }
    repositories {
        // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            stagingProfileId.set(System.getenv("OSSRH_STAGING_PROFILE_ID"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
        }
    }
}
