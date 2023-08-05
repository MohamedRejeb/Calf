plugins {
//    id("root.publication")
//    id("module.publication")
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinNativeCocoapods).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
//    kotlin("native.cocoapods") version "1.9.0" apply false
}
