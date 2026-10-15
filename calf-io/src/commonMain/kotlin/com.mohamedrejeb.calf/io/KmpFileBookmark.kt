package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext

/**
 * Keeps the app's access to this picked file or folder after the app restarts, and returns a
 * bookmark to store, for example in preferences. Reopen it with [fromBookmark].
 *
 * - **Android**: takes a persisted permission for the picked `content:` URI. Files and folders
 *   picked with the document picker support this; a URI without a persistable grant returns `null`.
 * - **iOS**: creates a bookmark of the location the user picked, so a folder outside the app's
 *   container stays readable. Picked files are copies the app owns; copy one into the app's
 *   storage to keep it.
 * - **Desktop**: the path, since the app can reopen any path it could open before.
 * - **JS** and **Wasm**: always `null`.
 *
 * Call [releaseBookmark] when the app no longer needs the access.
 *
 * @param context The platform context.
 * @return The bookmark, or `null` if access to this file cannot be kept.
 */
expect fun KmpFile.createBookmark(context: PlatformContext): String?

/**
 * Reopens a file or folder from a bookmark returned by [createBookmark].
 *
 * On iOS the reopened file holds its security-scoped access until [releaseBookmark] is called, so
 * the folder and the files inside it can be used through its URL or path. A bookmark can go
 * stale when the item is moved or renamed: call [createBookmark] on the reopened file and store
 * the new bookmark each time.
 *
 * @param bookmark A bookmark returned by [createBookmark].
 * @param context The platform context.
 * @return The file, or `null` if access to it was lost: the permission was released or revoked,
 * the item was deleted, or [bookmark] is not a bookmark.
 */
expect fun KmpFile.Companion.fromBookmark(bookmark: String, context: PlatformContext): KmpFile?

/**
 * Gives up the access kept by [createBookmark]: Android releases the persisted permission, and
 * iOS stops the access held by a file reopened with [fromBookmark]. Afterwards, bookmarks of this
 * file no longer reopen it on Android.
 *
 * @param context The platform context.
 */
expect fun KmpFile.releaseBookmark(context: PlatformContext)

// --- Context-free overloads ---

/**
 * Keeps the app's access to this picked file or folder after the app restarts; see the overload
 * that accepts a [PlatformContext].
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 */
fun KmpFile.createBookmark(): String? = createBookmark(resolveContext())

/**
 * Reopens a file or folder from a bookmark returned by [createBookmark]; see the overload that
 * accepts a [PlatformContext].
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 */
fun KmpFile.Companion.fromBookmark(bookmark: String): KmpFile? = fromBookmark(bookmark, resolveContext())

/**
 * Gives up the access kept by [createBookmark]; see the overload that accepts a [PlatformContext].
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 */
fun KmpFile.releaseBookmark(): Unit = releaseBookmark(resolveContext())
