plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinJvm).apply(false)
    alias(libs.plugins.kotlinSerialization).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.vanniktech.maven.publish).apply(false)
}

val calfVersion = System.getenv("VERSION") ?: "${libs.versions.calf.get()}-SNAPSHOT"

allprojects {
    group = "com.mohamedrejeb.calf"
    version = calfVersion
}
