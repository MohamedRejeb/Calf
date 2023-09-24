package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.io.KmpFile

@Composable
expect fun rememberFilePickerLauncher(
    type: FilePickerFileType = FilePickerFileType.All,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Single,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher

sealed class FilePickerFileType(vararg val value: String) {
    data object Image: FilePickerFileType(ImageContentType)
    data object Video: FilePickerFileType(VideoContentType)
    data object ImageVideo: FilePickerFileType(ImageContentType, VideoContentType)
    data object Audio: FilePickerFileType(AudioContentType)
    data object Document: FilePickerFileType(DocumentContentType)
    data object Presentation: FilePickerFileType(
        OpenDocumentPresentationContentType,
        PowerPointPresentationContentType,
        PowerPointOpenXmlPresentationContentType,
    )
    data object Spreadsheet: FilePickerFileType(
        OpenDocumentSpreadsheetContentType,
        ExcelSpreadsheetContentType,
        ExcelSpreadsheetOpenXmlContentType,
    )
    data object Word: FilePickerFileType(
        OpenDocumentTextContentType,
        WordDocumentContentType,
        WordOpenXmlDocumentContentType,
    )
    data object Pdf: FilePickerFileType(PdfContentType)
    data object Text: FilePickerFileType(TextContentType)
    data object Folder: FilePickerFileType("folder")
    data object All: FilePickerFileType(AllContentType)

    /**
     * Custom file type
     *
     * @param contentType List of content types
     */
    data class Custom(val contentType: List<String>): FilePickerFileType(*contentType.toTypedArray())

    companion object {
        const val FolderContentType = "folder"
        const val AudioContentType = "audio/*"
        const val ImageContentType = "image/*"
        const val VideoContentType = "video/*"
        const val PdfContentType = "application/pdf"

        // Presentations

        /**
         * OpenDocument presentation document
         *
         * Extension: .odp
         */
        const val OpenDocumentPresentationContentType = "application/vnd.oasis.opendocument.presentation"

        /**
         * PowerPoint presentation document
         *
         * Extension: .ppt
         */
        const val PowerPointPresentationContentType = "application/vnd.ms-powerpoint"

        /**
         * PowerPoint Open XML presentation document
         *
         * Extension: .pptx
         */
        const val PowerPointOpenXmlPresentationContentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation"

        // Spreadsheets

        /**
         * OpenDocument spreadsheet document
         *
         * Extension: .ods
         */
        const val OpenDocumentSpreadsheetContentType = "application/vnd.oasis.opendocument.spreadsheet"

        /**
         * Excel spreadsheet document
         *
         * Extension: .xlsx
         */
        const val ExcelSpreadsheetContentType = "application/vnd.ms-excel"

        /**
         * Excel Open XML spreadsheet document
         *
         * Extension: .xlsx
         */
        const val ExcelSpreadsheetOpenXmlContentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

        // Word

        /**
         * OpenDocument text document
         *
         * Extension: .odt
         */
        const val OpenDocumentTextContentType = "application/vnd.oasis.opendocument.text"

        /**
         * Word document
         *
         * Extension: .doc
         */
        const val WordDocumentContentType = "application/msword"

        /**
         * Word Open XML document
         *
         * Extension: .docx
         */
        const val WordOpenXmlDocumentContentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

        const val TextContentType = "text/*"
        const val DocumentContentType = "application/*"
        const val AllContentType = "*/*"
    }
}

sealed class FilePickerSelectionMode {
    data object Single: FilePickerSelectionMode()
    data object Multiple: FilePickerSelectionMode()
}

expect class FilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onLaunch: () -> Unit,
) {
    fun launch()
}