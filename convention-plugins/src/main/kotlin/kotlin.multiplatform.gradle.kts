plugins {
    `kotlin-multiplatform`
    id("android.library")
}

kotlin {
    applyHierarchyTemplate()
    applyTargets()
}