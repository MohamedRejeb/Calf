package com.mohamedrejeb.calf.io

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import org.robolectric.Robolectric
import java.io.File

/**
 * A provider over a folder that answers like a system `DocumentsProvider`: document and
 * tree-plus-document URIs are served, while a bare tree URI (what a folder pick returns) is refused
 * with the same UnsupportedOperationException. A real DocumentsProvider cannot be used here because
 * Robolectric calls the query overload that DocumentsProvider makes final and rejects.
 * Document ids are paths under [root].
 */
class FakeDocumentsProvider : ContentProvider() {
    lateinit var root: File

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val documentId = documentIdOf(uri)
        val columns = projection ?: DOCUMENT_COLUMNS
        val file = fileOf(documentId)
        return MatrixCursor(columns).apply {
            if (file.exists()) addRow(columns.map { column -> file.valueOf(column, documentId) }.toTypedArray())
        }
    }

    /** Like DocumentsProvider.getType: no type for a bare tree URI, rather than an exception. */
    override fun getType(uri: Uri): String? =
        if (uri.isBareTree()) null else fileOf(documentIdOf(uri)).takeIf { it.exists() }?.mimeType()

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor =
        ParcelFileDescriptor.open(fileOf(documentIdOf(uri)), ParcelFileDescriptor.parseMode(mode))

    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException()

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException()

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException()

    private fun documentIdOf(uri: Uri): String {
        val segments = uri.pathSegments
        return when {
            segments.size == 2 && segments[0] == PATH_DOCUMENT -> segments[1]
            segments.size == 4 && segments[0] == PATH_TREE && segments[2] == PATH_DOCUMENT -> segments[3]
            else -> throw UnsupportedOperationException("Unsupported Uri $uri")
        }
    }

    private fun Uri.isBareTree(): Boolean = pathSegments.size == 2 && pathSegments[0] == PATH_TREE

    private fun fileOf(documentId: String): File = root.resolve(documentId)

    private fun File.mimeType(): String = if (isDirectory) Document.MIME_TYPE_DIR else "application/octet-stream"

    private fun File.valueOf(column: String, documentId: String): Any? = when (column) {
        Document.COLUMN_DOCUMENT_ID -> documentId
        Document.COLUMN_DISPLAY_NAME -> name
        Document.COLUMN_SIZE -> length()
        Document.COLUMN_MIME_TYPE -> mimeType()
        Document.COLUMN_FLAGS -> Document.FLAG_SUPPORTS_WRITE or Document.FLAG_SUPPORTS_DELETE
        Document.COLUMN_LAST_MODIFIED -> lastModified()
        else -> null
    }

    companion object {
        const val AUTHORITY = "com.mohamedrejeb.calf.io.test.documents"

        private const val PATH_DOCUMENT = "document"
        private const val PATH_TREE = "tree"

        private val DOCUMENT_COLUMNS = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_SIZE,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_FLAGS,
            Document.COLUMN_LAST_MODIFIED,
        )

        fun install(root: File): FakeDocumentsProvider {
            val info = ProviderInfo().apply { authority = AUTHORITY }
            return Robolectric.buildContentProvider(FakeDocumentsProvider::class.java).create(info).get()
                .apply { this.root = root }
        }
    }
}
