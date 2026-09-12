package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.readByteArray
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val FILE_SIZE = 100_003
private const val CHUNK_SIZE = 4_096L

class KmpFileSourceTest {

    private val content = ByteArray(FILE_SIZE) { index -> (index % 251).toByte() }

    private val file: File = File.createTempFile("calf-source-test", ".bin").apply {
        writeBytes(content)
    }

    @AfterTest
    fun cleanUp() {
        file.delete()
    }

    @Test
    fun `source streams the whole file content`() = runBlocking {
        val bytes = KmpFile(file).source(PlatformContext.INSTANCE).buffered().use { source ->
            source.readByteArray()
        }

        assertContentEquals(content, bytes)
    }

    @Test
    fun `source can be consumed in chunks smaller than the file`() = runBlocking {
        val chunk = Buffer()
        var total = 0L
        var largestRead = 0L

        KmpFile(file).source(PlatformContext.INSTANCE).use { source ->
            while (true) {
                val read = source.readAtMostTo(chunk, CHUNK_SIZE)
                if (read == -1L) break
                largestRead = maxOf(largestRead, read)
                total += read
                chunk.clear()
            }
        }

        assertEquals(FILE_SIZE.toLong(), total)
        assertTrue(largestRead <= CHUNK_SIZE)
    }

    @Test
    fun `context-free overload streams the file on desktop`() = runBlocking {
        val bytes = KmpFile(file).source().buffered().use { it.readByteArray() }

        assertContentEquals(content, bytes)
    }

    @Test
    fun `source fails with FileNotFoundException for a missing file`() = runBlocking {
        val missing = File(file.parentFile, "calf-source-missing-${file.name}")

        assertFailsWith<FileNotFoundException> {
            KmpFile(missing).source(PlatformContext.INSTANCE)
        }
        Unit
    }

    @Test
    fun `source of an empty file reports end of stream immediately`() = runBlocking {
        val empty = File.createTempFile("calf-source-empty", ".bin")
        try {
            val read = KmpFile(empty).source(PlatformContext.INSTANCE).use { source ->
                source.readAtMostTo(Buffer(), CHUNK_SIZE)
            }

            assertEquals(-1L, read)
        } finally {
            empty.delete()
        }
    }

    @Test
    fun `use closes the underlying stream`() = runBlocking {
        val source = KmpFile(file).source(PlatformContext.INSTANCE)
        source.use { }

        assertFailsWith<IOException> {
            source.readAtMostTo(Buffer(), CHUNK_SIZE)
        }
        Unit
    }
}
