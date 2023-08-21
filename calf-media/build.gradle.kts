plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
//    id("module.publication")
}

kotlin {
    targetHierarchy.default()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    jvm("desktop") {
        jvmToolchain(11)
    }
//    js(IR) {
//        browser()
//    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets.commonMain.get().dependencies {
        //put your multiplatform dependencies here
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
    }
    sourceSets.commonTest.get().dependencies {
        implementation(libs.kotlin.test)
    }
}

android {
    namespace = "com.mohamedrejeb.calf.sf.symbols"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
