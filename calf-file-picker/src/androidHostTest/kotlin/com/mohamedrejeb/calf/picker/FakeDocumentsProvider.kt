package com.mohamedrejeb.calf.picker

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import org.robolectric.Robolectric
import java.io.File

/**
 * A provider over a folder that answers document URIs like a system `DocumentsProvider`, including
 * `DocumentsContract.deleteDocument`. A real DocumentsProvider cannot be used here because
 * Robolectric calls the query overload that DocumentsProvider makes final and rejects.
 * Document ids are file names under [root].
 */
class FakeDocumentsProvider : ContentProvider() {
    lateinit var root: File

    /** Like providers that only accept "w": opening with "wt" fails. */
    var refusesTruncation = false

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val columns = projection ?: arrayOf(Document.COLUMN_SIZE)
        val file = fileOf(uri)
        return MatrixCursor(columns).apply {
            if (file.exists()) addRow(columns.map { column -> file.valueOf(column) }.toTypedArray())
        }
    }

    override fun getType(uri: Uri): String? =
        fileOf(uri).takeIf { it.exists() }?.let { if (it.isDirectory) Document.MIME_TYPE_DIR else "application/pdf" }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (refusesTruncation && 't' in mode) throw IllegalArgumentException("Unsupported mode: $mode")
        return ParcelFileDescriptor.open(fileOf(uri), ParcelFileDescriptor.parseMode(mode))
    }

    /** DocumentsContract.deleteDocument arrives here, as it does on a DocumentsProvider. */
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        check(method == METHOD_DELETE_DOCUMENT) { "Unsupported call $method" }
        @Suppress("DEPRECATION")
        val document = requireNotNull(extras?.getParcelable<Uri>(EXTRA_URI))
        fileOf(document).delete()
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException()

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException()

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException()

    fun documentUri(name: String): Uri = DocumentsContract.buildDocumentUri(AUTHORITY, name)

    private fun fileOf(uri: Uri): File = root.resolve(DocumentsContract.getDocumentId(uri))

    private fun File.valueOf(column: String): Any? = when (column) {
        Document.COLUMN_DISPLAY_NAME -> name
        Document.COLUMN_SIZE -> length()
        Document.COLUMN_MIME_TYPE -> if (isDirectory) Document.MIME_TYPE_DIR else "application/pdf"
        else -> null
    }

    companion object {
        const val AUTHORITY = "com.mohamedrejeb.calf.picker.test.documents"

        /** The hidden DocumentsContract constants that deleteDocument uses. */
        private const val METHOD_DELETE_DOCUMENT = "android:deleteDocument"
        private const val EXTRA_URI = "uri"

        fun install(root: File): FakeDocumentsProvider {
            val info = ProviderInfo().apply { authority = AUTHORITY }
            return Robolectric.buildContentProvider(FakeDocumentsProvider::class.java).create(info).get()
                .apply { this.root = root }
        }
    }
}
