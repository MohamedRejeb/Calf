package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.io.RawSource

/**
 * Opens the content of the KmpFile as a kotlinx-io [RawSource] for streaming reads, so large
 * files can be processed without loading them into memory.
 *
 * Wrap the result with `buffered()` to get a [kotlinx.io.Source], and always close it, for
 * example with `use { }`.
 *
 * Platform behaviour:
 * - **Android**: opened through `ContentResolver.openInputStream`, so content URIs from the
 *   Storage Access Framework, Google Drive or Downloads stream directly even though [getPath]
 *   returns a URI rather than a file system path.
 * - **iOS** and **Desktop**: opened from the file system.
 * - **JS** and **Wasm**: browsers offer no synchronous file reads, so the whole file is read
 *   into memory first and served from a [kotlinx.io.Buffer].
 *
 * Opening and reading are blocking I/O on Android, iOS and Desktop. Call `source()` and
 * consume the returned source from a background dispatcher such as `Dispatchers.IO` or
 * `Dispatchers.Default`, never from the main thread.
 *
 * @param context The platform context used to open the file.
 * @return A [RawSource] positioned at the start of the file.
 * @throws kotlinx.io.files.FileNotFoundException if the file cannot be opened.
 */
expect suspend fun KmpFile.source(context: PlatformContext): RawSource

/**
 * Opens the content of the KmpFile as a kotlinx-io [RawSource] for streaming reads.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a
 * [PlatformContext].
 *
 * @see source
 */
suspend fun KmpFile.source(): RawSource = source(resolveContext())
