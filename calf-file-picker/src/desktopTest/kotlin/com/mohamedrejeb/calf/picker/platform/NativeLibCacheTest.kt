package com.mohamedrejeb.calf.picker.platform

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NativeLibCacheTest {

    private lateinit var cacheDir: File

    @BeforeTest
    fun setUp() {
        cacheDir = createTempDirectory("calf-native-cache-test").toFile()
    }

    @AfterTest
    fun tearDown() {
        cacheDir.deleteRecursively()
    }

    @Test
    fun `cache file name inserts the hash before the extension`() {
        assertEquals(
            "calf_filepicker_native-0123456789abcdef.dll",
            nativeLibraryCacheFileName("calf_filepicker_native.dll", "0123456789abcdef"),
        )
        assertEquals(
            "libcalf_filepicker_native-0123456789abcdef.dylib",
            nativeLibraryCacheFileName("libcalf_filepicker_native.dylib", "0123456789abcdef"),
        )
        assertEquals(
            "libcalf_filepicker_native-0123456789abcdef.so",
            nativeLibraryCacheFileName("libcalf_filepicker_native.so", "0123456789abcdef"),
        )
    }

    @Test
    fun `content hash is deterministic, differs per content and is 16 hex chars`() {
        val first = nativeLibraryContentHash(byteArrayOf(1, 2, 3))
        val same = nativeLibraryContentHash(byteArrayOf(1, 2, 3))
        val other = nativeLibraryContentHash(byteArrayOf(1, 2, 4))

        assertEquals(first, same)
        assertNotEquals(first, other)
        assertTrue(Regex("[0-9a-f]{16}").matches(first), "unexpected hash format: $first")
    }

    @Test
    fun `extract writes the file with the exact bytes when it is missing`() {
        val bytes = byteArrayOf(10, 20, 30, 40)

        val loaded = extractNativeLibrary(bytes, cacheDir, "lib-aaaa.so")

        assertEquals(File(cacheDir, "lib-aaaa.so"), loaded)
        assertContentEquals(bytes, loaded.readBytes())
    }

    @Test
    fun `extract reuses an existing identical file without rewriting it`() {
        val bytes = byteArrayOf(7, 8, 9)
        val target = File(cacheDir, "lib-bbbb.so").apply { writeBytes(bytes) }
        val pastMillis = 1_000_000_000_000L
        assertTrue(target.setLastModified(pastMillis))

        val loaded = extractNativeLibrary(bytes, cacheDir, "lib-bbbb.so")

        assertEquals(target, loaded)
        assertEquals(pastMillis, loaded.lastModified())
        assertContentEquals(bytes, loaded.readBytes())
    }

    @Test
    fun `extract replaces an existing file whose bytes differ`() {
        val target = File(cacheDir, "lib-cccc.so").apply { writeBytes(byteArrayOf(1, 1, 1)) }
        val bytes = byteArrayOf(2, 2, 2, 2)

        val loaded = extractNativeLibrary(bytes, cacheDir, "lib-cccc.so")

        assertEquals(target, loaded)
        assertContentEquals(bytes, loaded.readBytes())
    }

    @Test
    fun `extract falls back to a unique file in the cache dir when the target cannot be replaced`() {
        val blockedTarget = File(cacheDir, "lib-dddd.so")
        assertTrue(blockedTarget.mkdir())
        assertTrue(File(blockedTarget, "occupied").createNewFile())
        val bytes = byteArrayOf(5, 5, 5)

        val loaded = extractNativeLibrary(bytes, cacheDir, "lib-dddd.so")

        assertNotEquals(blockedTarget, loaded)
        assertEquals(cacheDir, loaded.parentFile)
        assertTrue(loaded.isFile)
        assertContentEquals(bytes, loaded.readBytes())
    }
}
