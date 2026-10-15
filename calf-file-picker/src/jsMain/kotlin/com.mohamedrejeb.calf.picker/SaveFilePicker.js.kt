package com.mohamedrejeb.calf.picker

import org.w3c.files.Blob
import org.w3c.files.File

internal actual fun isSaveFilePickerSupported(): Boolean =
    js("typeof window !== 'undefined' && typeof window.showSaveFilePicker === 'function'")

internal actual fun showSaveFilePicker(
    blob: Blob,
    suggestedName: String,
    mimeType: String,
    extension: String,
    onSaved: (File) -> Unit,
    onCancelled: () -> Unit,
    onPickerFailed: () -> Unit,
    onWriteFailed: () -> Unit,
) {
    js(
        """
        var options = { suggestedName: suggestedName };
        if (extension && mimeType !== 'application/octet-stream') {
            var accept = {};
            accept[mimeType] = ['.' + extension];
            options.types = [{ accept: accept }];
        }
        window.showSaveFilePicker(options).then(
            function (handle) {
                handle.createWritable()
                    .then(function (writable) {
                        return writable.write(blob).then(function () { return writable.close(); });
                    })
                    .then(function () { return handle.getFile(); })
                    .then(function (file) { onSaved(file); }, function () { onWriteFailed(); });
            },
            function (error) {
                if (error && error.name === 'AbortError') onCancelled(); else onPickerFailed();
            }
        );
        """,
    )
}
