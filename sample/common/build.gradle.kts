plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("org.jetbrains.compose")
    id("com.android.library")
}

kotlin {
    androidTarget {
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

    cocoapods {
        summary = "Calf"
        homepage = "https://github.com/MohamedRejeb/Calf"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../ios/Podfile")
        framework {
            baseName = "Common"
            isStatic = true

            // IMPORTANT: Exporting calf-ui is required for some functionalities to work
//            export(project(":calf-ui"))
            export("com.mohamedrejeb.calf:calf-ui:0.1.1")
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // Calf
                api("com.mohamedrejeb.calf:calf-ui:0.1.1")
                implementation("com.mohamedrejeb.calf:calf-file-picker:0.1.1")
//                api(project(":calf-ui"))
//                implementation(project(":calf-file-picker"))
                implementation(project(":calf-navigation"))
            }
        }

        val androidMain by getting {
            dependencies {}
        }

        val desktopMain by getting {
            dependencies {}
        }

//        val jsMain by getting {
//            dependencies {}
//        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {}
        }
    }
}

android {
    namespace = "com.mohamedrejeb.calf.sample.common"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}