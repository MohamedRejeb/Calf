package com.mohamedrejeb.calf.picker

import android.net.Uri
import androidx.compose.runtime.saveable.Saver
import com.mohamedrejeb.calf.io.KmpFile

/** What a file saver writes once the user has picked where to save it. */
internal sealed interface SaveSource {
    class Bytes(val bytes: ByteArray) : SaveSource

    class File(val file: KmpFile) : SaveSource
}

/**
 * Holds the source of the save in flight between `launch` and the system result.
 *
 * The system save sheet pauses and resumes the activity, so the result is handled by a later
 * composition than the one that launched the save. The source therefore lives in this holder,
 * which every composition shares, rather than in a composition-local variable.
 */
internal class PendingSaveSource {
    private var source: SaveSource? = null

    fun set(source: SaveSource) {
        this.source = source
    }

    /** Returns the pending source and clears it, so a result is never written twice. */
    fun take(): SaveSource? = source.also { source = null }

    companion object {
        /**
         * Keeps a pending file across process death by its URI. Pending bytes are not saved: they
         * can be larger than the saved instance state allows.
         */
        val Saver: Saver<PendingSaveSource, String> = Saver(
            save = { holder -> (holder.source as? SaveSource.File)?.file?.uri?.toString() },
            restore = { uri -> PendingSaveSource().apply { set(SaveSource.File(KmpFile(Uri.parse(uri)))) } },
        )
    }
}
