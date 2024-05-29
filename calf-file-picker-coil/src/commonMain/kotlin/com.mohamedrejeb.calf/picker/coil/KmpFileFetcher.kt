package com.mohamedrejeb.calf.picker.coil

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.core.PlatformContext
import okio.Buffer

expect fun coil3.PlatformContext.toCalfPlatformContext(): PlatformContext

class KmpFileFetcher(
    private val file: KmpFile,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        return SourceFetchResult(
            source = ImageSource(
                source = Buffer().apply {
                    write(file.readByteArray(options.context.toCalfPlatformContext()))
                },
                fileSystem = options.fileSystem,
            ),
            mimeType = null,
            dataSource = DataSource.MEMORY,
        )
    }

    class Factory : Fetcher.Factory<KmpFile> {
        override fun create(
            data: KmpFile,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher {
            return KmpFileFetcher(data, options)
        }
    }
}