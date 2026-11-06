package com.mohamedrejeb.calf.io

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.mohamedrejeb.calf.core.PlatformContext
import java.io.File
import java.io.FileNotFoundException

actual class KmpFile(
    val uri: Uri,
) {
    actual companion object
}

actual fun KmpFile.exists(context: PlatformContext): Boolean =
    try {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        inputStream.close()

        true
    } catch (e: Exception) {
        false
    }

/**
 * Reads the content of the file as a byte array
 *
 * @param context the context to use to open the file
 * @throws FileNotFoundException if the file is not found
 * @return the content of the file as a byte array
 */
@Throws(FileNotFoundException::class)
actual suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray {
    val inputStream =
        context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException("File not found")

    return inputStream.readBytes().also {
        inputStream.close()
    }
}

actual fun KmpFile.getName(context: PlatformContext): String? {
    val documentFile = DocumentFile.fromSingleUri(context, queryUri)

    return documentFile?.name
}

/**
 * The URI to ask the provider about. A picked folder is a bare tree URI, which documents providers
 * refuse to query, so the question goes to the document URI of the tree's root instead.
 */
private val KmpFile.queryUri: Uri
    get() = uri.takeIf { it.isBareTreeUri() }
        ?.let { DocumentsContract.buildDocumentUriUsingTree(it, DocumentsContract.getTreeDocumentId(it)) }
        ?: uri

private fun Uri.isBareTreeUri(): Boolean =
    scheme == ContentResolver.SCHEME_CONTENT && pathSegments.size == 2 && pathSegments[0] == TREE_PATH_SEGMENT

/** A `content:` URI has no file system path, so only `file:` URIs have one. */
actual fun KmpFile.getPath(context: PlatformContext): String? =
    uri.takeIf { it.scheme == ContentResolver.SCHEME_FILE }?.path

actual fun KmpFile.toUriString(): String? = uri.toString()

actual fun KmpFile.Companion.fromUriString(uriString: String): KmpFile? = when {
    uriString.startsWith(PATH_SEPARATOR) -> KmpFile(Uri.fromFile(File(uriString)))
    else -> Uri.parse(uriString).takeIf { it.scheme in FILE_REFERENCE_SCHEMES }?.let(::KmpFile)
}

actual fun KmpFile.getSize(context: PlatformContext): Long? = when (uri.scheme) {
    ContentResolver.SCHEME_FILE -> uri.path?.let(::File)?.takeIf { it.isFile }?.length()
    else -> queryProviderSize(context)
}

/** Asks the provider; it may not know the size, and a folder has none. */
private fun KmpFile.queryProviderSize(context: PlatformContext): Long? {
    if (isDirectory(context)) return null
    return try {
        context.contentResolver.query(queryUri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val column = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (column >= 0 && cursor.moveToFirst() && !cursor.isNull(column)) cursor.getLong(column) else null
        }
    } catch (e: RuntimeException) {
        // Providers refuse a query in their own ways (SecurityException, IllegalArgumentException,
        // UnsupportedOperationException); the size is then unknown, which is what null means.
        null
    }
}

actual fun KmpFile.getMimeType(context: PlatformContext): String? {
    val providerType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        try {
            context.contentResolver.getType(queryUri)
        } catch (e: RuntimeException) {
            // Unknown to the provider: fall back to the extension.
            null
        }
    } else {
        null
    }
    return providerType ?: mimeTypeFromName(context)
}

private fun KmpFile.mimeTypeFromName(context: PlatformContext): String? {
    val name = if (uri.scheme == ContentResolver.SCHEME_FILE) uri.lastPathSegment else getName(context)
    val extension = name?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()
    return extension?.takeIf { it.isNotEmpty() }?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
}

private const val PATH_SEPARATOR = "/"

/** The first path segment of a tree URI, as built by DocumentsContract.buildTreeDocumentUri. */
private const val TREE_PATH_SEGMENT = "tree"

private val FILE_REFERENCE_SCHEMES = setOf(ContentResolver.SCHEME_CONTENT, ContentResolver.SCHEME_FILE)

actual fun KmpFile.isDirectory(context: PlatformContext): Boolean {
    val documentFile = DocumentFile.fromSingleUri(context, queryUri)

    return documentFile?.isDirectory == true
}

/**
 * Converts this [Uri] to a [KmpFile].
 */
fun Uri.toKmpFile(): KmpFile = KmpFile(this)

/**
 * Converts this [java.io.File] to a [KmpFile] by converting to a file [Uri].
 */
fun java.io.File.toKmpFile(): KmpFile = KmpFile(Uri.fromFile(this))
