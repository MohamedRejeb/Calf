package com.mohamedrejeb.calf.picker

/**
 * A file picker or file saver could not complete. Cancelling a dialog is not a failure: it is
 * reported through `onResult`.
 *
 * @param message What failed, for logs; it is not meant to be shown to users as is.
 * @param cause The underlying platform error, if any.
 */
class FilePickerException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * The failure to report when [failed] of [picked] files could not be read, or `null` when all of
 * them could. The files that could be read are still delivered.
 */
internal fun partialPickFailure(failed: Int, picked: Int): FilePickerException? =
    if (failed == 0) null else FilePickerException("$failed of $picked picked files could not be read")
