import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

val javaVersionString = libs.versions.java.get()

android {
    namespace = "com.mohamedrejeb.calf.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.sample.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()

        applicationId = "com.mohamedrejeb.calf.android"
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    compileOptions {
        val javaVersion = JavaVersion.toVersion(javaVersionString.toInt())
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlin {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaVersionString))
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    dependencies {
        implementation(projects.sample.common)

        implementation(libs.activity.compose)
        implementation(libs.compose.tooling.preview)
        implementation(libs.appcompat)
        debugImplementation(libs.compose.tooling)
    }
}