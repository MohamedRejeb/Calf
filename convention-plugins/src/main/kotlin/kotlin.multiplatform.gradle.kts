plugins {
    `kotlin-multiplatform`
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    applyHierarchyTemplate()
    applyTargets()
}

androidLibrarySetup()
setJvmTarget()
