package com.mohamedrejeb.calf.share.platform

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private const val LIB_NAME = "calf_share_native"

/**
 * Loads the native share library from JAR resources.
 *
 * The library is expected at: native/<os>-<arch>/<libFileName>
 * It is extracted to a user-scoped cache directory and loaded via [System.load].
 */
internal fun loadNativeLibrary() {
    val osName = System.getProperty("os.name")?.lowercase().orEmpty()
    val osArch = System.getProperty("os.arch")?.lowercase().orEmpty()

    val (osPart, libFileName) = when {
        "mac" in osName || "darwin" in osName ->
            "macos" to "lib$LIB_NAME.dylib"

        "win" in osName ->
            "windows" to "$LIB_NAME.dll"

        "nux" in osName || "nix" in osName ->
            "linux" to "lib$LIB_NAME.so"

        else -> error("Unsupported OS: $osName")
    }

    val archPart = when {
        osArch == "aarch64" || osArch == "arm64" -> "arm64"
        osArch == "amd64" || osArch == "x86_64" -> "x64"
        else -> error("Unsupported architecture: $osArch")
    }

    val resourcePath = "native/$osPart-$archPart/$libFileName"

    val inputStream: InputStream = NativeShareBridge::class.java.classLoader
        ?.getResourceAsStream(resourcePath)
        ?: error(
            "Native library not found in JAR resources at '$resourcePath'. " +
                "Ensure the native library is built for $osPart-$archPart."
        )

    val userHome = System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir")
    val cacheDir = File(userHome, ".cache/calf-share")
    cacheDir.mkdirs()

    val targetFile = File(cacheDir, libFileName)

    inputStream.use { input ->
        val bytes = input.readBytes()
        val tempFile = Files.createTempFile(cacheDir.toPath(), "calf-native-", ".tmp")
        try {
            Files.write(tempFile, bytes)
            try {
                Files.move(
                    tempFile,
                    targetFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: Exception) {
                Files.move(tempFile, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Exception) {
            Files.deleteIfExists(tempFile)
            error("Failed to extract native share library to '$targetFile': ${e.message}")
        }
    }

    @Suppress("UnsafeDynamicallyLoadedCode")
    System.load(targetFile.absolutePath)
}
