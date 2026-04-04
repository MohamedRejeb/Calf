plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfIo)

        implementation(libs.compose.foundation)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
    }

    sourceSets.webMain.dependencies {
        implementation(libs.kotlinx.browser)
    }
}

// ---------------------------------------------------------------------------
// Rust native library build tasks (macOS share sheet via NSSharingServicePicker)
// ---------------------------------------------------------------------------

val nativeDir = layout.projectDirectory.dir("native")
val nativeOutputDir = layout.buildDirectory.dir("native-libs")

val prebuiltNativeDir = layout.projectDirectory.dir("src/desktopMain/resources/native")
val hasPrebuiltNativeLibs = prebuiltNativeDir.asFile.exists() &&
    prebuiltNativeDir.asFile.listFiles()?.any { it.isDirectory && it.listFiles()?.isNotEmpty() == true } == true

if (!hasPrebuiltNativeLibs) {
    fun nativeLibProps(): Triple<String, String, String> {
        val osName = System.getProperty("os.name").lowercase()
        val osArch = System.getProperty("os.arch").lowercase()

        return when {
            osName.contains("mac") && (osArch == "aarch64" || osArch == "arm64") ->
                Triple("aarch64-apple-darwin", "macos-arm64", "libcalf_share_native.dylib")

            osName.contains("mac") ->
                Triple("x86_64-apple-darwin", "macos-x64", "libcalf_share_native.dylib")

            osName.contains("win") && osArch == "amd64" ->
                Triple("x86_64-pc-windows-msvc", "windows-x64", "calf_share_native.dll")

            osName.contains("nux") && osArch == "amd64" ->
                Triple("x86_64-unknown-linux-gnu", "linux-x64", "libcalf_share_native.so")

            osName.contains("nux") && (osArch == "aarch64" || osArch == "arm64") ->
                Triple("aarch64-unknown-linux-gnu", "linux-arm64", "libcalf_share_native.so")

            else -> error("Unsupported host: $osName $osArch")
        }
    }

    val buildNativeLib by tasks.registering(Exec::class) {
        group = "native"
        description = "Build the Rust native share library for the current host."

        workingDir = nativeDir.asFile

        val (rustTarget, _, _) = nativeLibProps()

        commandLine("cargo", "build", "--release", "--target", rustTarget)

        outputs.upToDateWhen { false }
    }

    val copyNativeLib by tasks.registering(Copy::class) {
        group = "native"
        description = "Copy the built native library into JAR resources."

        dependsOn(buildNativeLib)

        val (rustTarget, osDirName, libFile) = nativeLibProps()

        from(nativeDir.file("target/$rustTarget/release/$libFile"))
        into(nativeOutputDir.map { it.dir("native/$osDirName") })
    }

    kotlin.sourceSets.desktopMain {
        resources.srcDir(nativeOutputDir)
    }

    tasks.matching { it.name.contains("desktopProcessResources", ignoreCase = true) }.configureEach {
        dependsOn(copyNativeLib)
    }
}
