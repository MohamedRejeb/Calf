package com.mohamedrejeb.calf.picker.platform

import java.io.File

private const val LIB_NAME = "calf_filepicker_native"
private const val CACHE_DIR = ".cache/calf-filepicker"

/**
 * Loads the native file picker library.
 *
 * The system library path is tried first (packagers like Conveyor extract natives
 * out of the jar). Otherwise the library is read from JAR resources at
 * `native/<os>-<arch>/<libFileName>`, cached under a content-hashed name in a
 * user-scoped directory, and loaded via [System.load].
 *
 * Called once from [NativeFilePickerBridge]'s object init block.
 */
internal fun loadNativeLibrary() {
    try {
        System.loadLibrary(LIB_NAME)
        return
    } catch (_: UnsatisfiedLinkError) {
    }

    val (resourcePath, libFileName) = bundledLibraryLocation()
    val bytes = NativeFilePickerBridge::class.java.classLoader
        ?.getResourceAsStream(resourcePath)
        ?.use { it.readBytes() }
        ?: error(
            "Native library not found in JAR resources at '$resourcePath'. " +
                "Ensure the native library is built for this platform."
        )

    val cacheFileName = nativeLibraryCacheFileName(libFileName, nativeLibraryContentHash(bytes))
    val libraryFile = try {
        extractNativeLibrary(bytes, userCacheDir(), cacheFileName)
    } catch (e: Exception) {
        error("Failed to extract native file picker library '$cacheFileName': ${e.message}")
    }

    @Suppress("UnsafeDynamicallyLoadedCode")
    System.load(libraryFile.absolutePath)
}

/** Resource path inside the JAR and the platform file name of the bundled library. */
private fun bundledLibraryLocation(): Pair<String, String> {
    val osName = System.getProperty("os.name")?.lowercase().orEmpty()
    val osArch = System.getProperty("os.arch")?.lowercase().orEmpty()

    val (osPart, libFileName) = when {
        "mac" in osName || "darwin" in osName -> "macos" to "lib$LIB_NAME.dylib"
        "win" in osName -> "windows" to "$LIB_NAME.dll"
        "nux" in osName || "nix" in osName -> "linux" to "lib$LIB_NAME.so"
        else -> error("Unsupported OS: $osName")
    }

    val archPart = when (osArch) {
        "aarch64", "arm64" -> "arm64"
        "amd64", "x86_64" -> "x64"
        else -> error("Unsupported architecture: $osArch")
    }

    return "native/$osPart-$archPart/$libFileName" to libFileName
}

/** User-scoped cache directory, avoiding the security risks of a shared /tmp. */
private fun userCacheDir(): File {
    val userHome = System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir")
    return File(userHome, CACHE_DIR)
}
