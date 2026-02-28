import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

android {
    namespace = "com.mohamedrejeb.calf.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()

        applicationId = "com.mohamedrejeb.calf.android"
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    // Migrated from kotlinOptions to compilerOptions DSL (required in Kotlin 2.3+)
    // See: https://kotlinlang.org/docs/gradle-compiler-options.html
    kotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
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