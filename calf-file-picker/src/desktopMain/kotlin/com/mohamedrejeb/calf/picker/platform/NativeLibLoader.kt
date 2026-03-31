package com.mohamedrejeb.calf.picker.platform

import java.io.File
import java.io.InputStream

private const val LIB_NAME = "calf_filepicker_native"

/**
 * Loads the native file picker library from JAR resources.
 *
 * The library is expected at: native/<os>-<arch>/<libFileName>
 * It is extracted to a temp directory and loaded via [System.load].
 */
internal fun loadNativeLibrary() {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()

    val (osPart, libFileName) = when {
        osName.contains("mac") || osName.contains("darwin") -> {
            "macos" to "lib$LIB_NAME.dylib"
        }
        osName.contains("win") -> {
            "windows" to "$LIB_NAME.dll"
        }
        osName.contains("nux") || osName.contains("nix") -> {
            "linux" to "lib$LIB_NAME.so"
        }
        else -> error("Unsupported OS: $osName")
    }

    val archPart = when {
        osArch == "aarch64" || osArch == "arm64" -> "arm64"
        osArch == "amd64" || osArch == "x86_64" -> "x64"
        else -> error("Unsupported architecture: $osArch")
    }

    val resourcePath = "native/$osPart-$archPart/$libFileName"

    val inputStream: InputStream = NativeFilePickerBridge::class.java.classLoader
        ?.getResourceAsStream(resourcePath)
        ?: error(
            "Native library not found in JAR resources at '$resourcePath'. " +
                "Ensure the native library is built for $osPart-$archPart."
        )

    val tempDir = File(System.getProperty("java.io.tmpdir"), "calf-filepicker-native")
    tempDir.mkdirs()

    val tempFile = File(tempDir, libFileName)

    // Only extract if not already present or size differs
    inputStream.use { input ->
        val bytes = input.readBytes()
        if (!tempFile.exists() || tempFile.length() != bytes.size.toLong()) {
            tempFile.writeBytes(bytes)
        }
    }

    @Suppress("UnsafeDynamicallyLoadedCode")
    System.load(tempFile.absolutePath)
}
