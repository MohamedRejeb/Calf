package com.mohamedrejeb.calf.picker.platform

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

private const val HASH_ALGORITHM = "SHA-256"
private const val HASH_LENGTH = 16
private const val TEMP_PREFIX = "calf-native-"
private const val TEMP_SUFFIX = ".tmp"

/**
 * Short, stable fingerprint of a native library's bytes.
 *
 * Used to name the cached copy so that different builds of the library
 * never share a file on disk.
 */
internal fun nativeLibraryContentHash(bytes: ByteArray): String =
    MessageDigest.getInstance(HASH_ALGORITHM)
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
        .take(HASH_LENGTH)

/**
 * Inserts [contentHash] before the extension of [libFileName],
 * e.g. `calf_filepicker_native.dll` -> `calf_filepicker_native-<hash>.dll`.
 */
internal fun nativeLibraryCacheFileName(libFileName: String, contentHash: String): String {
    val extension = libFileName.substringAfterLast('.', missingDelimiterValue = "")
    val baseName = libFileName.substringBeforeLast('.')
    return if (extension.isEmpty()) "$baseName-$contentHash" else "$baseName-$contentHash.$extension"
}

/**
 * Makes [bytes] available as a file inside [cacheDir] and returns the file to load.
 *
 * - An existing target with identical content is reused untouched, so a library
 *   that another process has already loaded (and locked, on Windows) is never
 *   replaced.
 * - Otherwise the bytes are written to a temp file and moved over the target.
 * - If the move fails for any reason, the unique temp file itself is returned
 *   and scheduled for deletion on exit, so loading still succeeds.
 */
internal fun extractNativeLibrary(bytes: ByteArray, cacheDir: File, fileName: String): File {
    cacheDir.mkdirs()
    val target = File(cacheDir, fileName)
    if (hasIdenticalContent(target, bytes)) return target

    val tempFile = Files.createTempFile(cacheDir.toPath(), TEMP_PREFIX, TEMP_SUFFIX)
    Files.write(tempFile, bytes)

    return runCatching { moveReplacing(tempFile, target.toPath()) }
        .map { target }
        .getOrElse { tempFile.toFile().also { it.deleteOnExit() } }
}

private fun hasIdenticalContent(file: File, bytes: ByteArray): Boolean =
    file.isFile &&
        file.length() == bytes.size.toLong() &&
        nativeLibraryContentHash(file.readBytes()) == nativeLibraryContentHash(bytes)

private fun moveReplacing(source: Path, target: Path) {
    try {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    } catch (_: Exception) {
        // ATOMIC_MOVE is not supported on every filesystem; retry with a plain replace.
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
    }
}
