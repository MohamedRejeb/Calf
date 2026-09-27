package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet

private const val BYTES_PER_KB = 1024L
private const val BYTES_PER_MB = BYTES_PER_KB * 1024
private const val BYTES_PER_GB = BYTES_PER_MB * 1024
private const val MIME_ANY = "*/*"

/**
 * What a file is, guessed from its extension: the icon and tint of its row in the file demos,
 * and the MIME type the share demo sends it with.
 */
internal enum class FileKind(
    val icon: ImageVector,
    val tint: Color,
    val mimeType: String,
    private val extensions: Set<String>,
) {
    Image(
        icon = Icons.Outlined.Image,
        tint = TileBlue,
        mimeType = "image/*",
        extensions = setOf("jpg", "jpeg", "png", "gif", "heic", "heif", "webp", "bmp", "svg", "tiff"),
    ),
    Video(
        icon = Icons.Outlined.Movie,
        tint = TileViolet,
        mimeType = "video/*",
        extensions = setOf("mp4", "mov", "m4v", "avi", "mkv", "webm"),
    ),
    Audio(
        icon = Icons.Outlined.MusicNote,
        tint = TileTeal,
        mimeType = "audio/*",
        extensions = setOf("mp3", "m4a", "wav", "aac", "flac", "ogg"),
    ),
    Pdf(
        icon = Icons.Outlined.PictureAsPdf,
        tint = TileCoral,
        mimeType = "application/pdf",
        extensions = setOf("pdf"),
    ),
    Document(
        icon = Icons.Outlined.Description,
        tint = TileAmber,
        mimeType = MIME_ANY,
        extensions = setOf("doc", "docx", "odt", "rtf", "pages", "xls", "xlsx", "ods", "numbers", "ppt", "pptx", "odp", "key"),
    ),
    Text(
        icon = Icons.AutoMirrored.Outlined.TextSnippet,
        tint = TileBlue,
        mimeType = "text/plain",
        extensions = setOf("txt", "md", "csv", "json", "xml", "log", "yml", "yaml"),
    ),
    Folder(
        icon = Icons.Outlined.Folder,
        tint = TileAmber,
        mimeType = MIME_ANY,
        extensions = emptySet(),
    ),
    Other(
        icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
        tint = TileIndigo,
        mimeType = MIME_ANY,
        extensions = emptySet(),
    );

    companion object {
        /** The kind of a file named [fileName], [Other] when the extension says nothing. */
        fun of(fileName: String?): FileKind {
            val extension = fileName?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()
            if (extension.isNullOrEmpty()) return Other
            return entries.firstOrNull { extension in it.extensions } ?: Other
        }
    }
}

/** The MIME type to share [kinds] under: their own when they agree, any type otherwise. */
internal fun sharedMimeType(kinds: List<FileKind>): String =
    kinds.distinct().singleOrNull()?.mimeType ?: MIME_ANY

/** The leading tile of a file row. */
@Composable
internal fun FileKindTile(kind: FileKind, modifier: Modifier = Modifier) {
    IconTile(icon = kind.icon, color = kind.tint, modifier = modifier)
}

/** "845 KB", "2.4 MB": a size the way a file browser writes it. */
internal fun formatFileSize(bytes: Long): String =
    when {
        bytes < BYTES_PER_KB -> "$bytes B"
        bytes < BYTES_PER_MB -> "${bytes / BYTES_PER_KB} KB"
        bytes < BYTES_PER_GB -> oneDecimal(bytes, BYTES_PER_MB, "MB")
        else -> oneDecimal(bytes, BYTES_PER_GB, "GB")
    }

private fun oneDecimal(bytes: Long, unit: Long, suffix: String): String {
    val tenths = bytes * 10 / unit
    return "${tenths / 10}.${tenths % 10} $suffix"
}
