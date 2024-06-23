package com.mohamedrejeb.calf.picker.platform.mac

import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.platform.PlatformFilePicker
import io.github.vinceglb.filekit.core.platform.mac.foundation.Foundation
import io.github.vinceglb.filekit.core.platform.mac.foundation.ID
import jodd.net.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Window
import java.io.File

internal class MacOSFilePicker: PlatformFilePicker {
	override suspend fun launchFilePicker(
		initialDirectory: String?,
		type: FilePickerFileType,
		selectionMode: FilePickerSelectionMode,
		title: String?,
		parentWindow: Window?,
		onResult: (List<File>) -> Unit,
	) =
		if (selectionMode == FilePickerSelectionMode.Single)
			callNativeMacOSPicker(
				mode = MacOSFilePickerMode.SingleFile,
				initialDirectory = initialDirectory,
				type = type,
				title = title,
				onResult = {
					onResult(listOfNotNull(it))
				},
			)
		else
			callNativeMacOSPicker(
				mode = MacOSFilePickerMode.MultipleFiles,
				initialDirectory = initialDirectory,
				type = type,
				title = title,
				onResult = {
					onResult(it.orEmpty())
				},
			)

	override suspend fun launchDirectoryPicker(
		initialDirectory: String?,
		title: String?,
		parentWindow: Window?,
		onResult: (File?) -> Unit,
	) {
		return callNativeMacOSPicker(
			mode = MacOSFilePickerMode.Directories,
			initialDirectory = initialDirectory,
			type = FilePickerFileType.Folder,
			title = title,
			onResult = onResult,
		)
	}

	private suspend fun <T> callNativeMacOSPicker(
		mode: MacOSFilePickerMode<T>,
		initialDirectory: String?,
		type: FilePickerFileType,
		title: String?,
		onResult: (T?) -> Unit,
	) = withContext(Dispatchers.Default) {
		val pool = Foundation.NSAutoreleasePool()
		try {
			var response: T? = null

			Foundation.executeOnMainThread(
				withAutoreleasePool = false,
				waitUntilDone = true,
			) {
				// Create the file picker
				val openPanel = Foundation.invoke("NSOpenPanel", "new")

				// Setup single, multiple selection or directory mode
				mode.setupPickerMode(openPanel)

				// Set the title
				title?.let {
					Foundation.invoke(openPanel, "setMessage:", Foundation.nsString(it))
				}

				// Set initial directory
				initialDirectory?.let {
					Foundation.invoke(openPanel, "setDirectoryURL:", Foundation.nsURL(it))
				}

				// Set file extensions
				if (type !is FilePickerFileType.Folder) {
					val extensions =
						if (type is FilePickerFileType.Extension)
							type.extensions
						else
							type.value
								.map {
									MimeTypes.findExtensionsByMimeTypes(it, it.contains('*'))
								}
								.flatten()
								.distinct()
					val items = extensions.map { Foundation.nsString(it) }
					val nsData = Foundation.invokeVarArg("NSArray", "arrayWithObjects:", *items.toTypedArray())
					Foundation.invoke(openPanel, "setAllowedFileTypes:", nsData)
				}

				// Open the file picker
				val result = Foundation.invoke(openPanel, "runModal")

				// Get the path(s) from the file picker if the user validated the selection
				if (result.toInt() == 1) {
					response = mode.getResult(openPanel)
				}
			}

			onResult(response)
		} finally {
			pool.drain()
		}
	}

	private companion object {
		fun singlePath(openPanel: ID): File? {
			val url = Foundation.invoke(openPanel, "URL")
			val nsPath = Foundation.invoke(url, "path")
			val path = Foundation.toStringViaUTF8(nsPath)
			return path?.let { File(it) }
		}

		fun multiplePaths(openPanel: ID): List<File>? {
			val urls = Foundation.invoke(openPanel, "URLs")
			val urlCount = Foundation.invoke(urls, "count").toInt()

			return (0 until urlCount).mapNotNull { index ->
				val url = Foundation.invoke(urls, "objectAtIndex:", index)
				val nsPath = Foundation.invoke(url, "path")
				val path = Foundation.toStringViaUTF8(nsPath)
				path?.let { File(it) }
			}.ifEmpty { null }
		}
	}

	private sealed class MacOSFilePickerMode<T> {
		abstract fun setupPickerMode(openPanel: ID)
		abstract fun getResult(openPanel: ID): T?

		data object SingleFile : MacOSFilePickerMode<File?>() {
			override fun setupPickerMode(openPanel: ID) {
				Foundation.invoke(openPanel, "setCanChooseFiles:", true)
				Foundation.invoke(openPanel, "setCanChooseDirectories:", false)
			}

			override fun getResult(openPanel: ID): File? = singlePath(openPanel)
		}

		data object MultipleFiles : MacOSFilePickerMode<List<File>>() {
			override fun setupPickerMode(openPanel: ID) {
				Foundation.invoke(openPanel, "setCanChooseFiles:", true)
				Foundation.invoke(openPanel, "setCanChooseDirectories:", false)
				Foundation.invoke(openPanel, "setAllowsMultipleSelection:", true)
			}

			override fun getResult(openPanel: ID): List<File>? = multiplePaths(openPanel)
		}

		data object Directories : MacOSFilePickerMode<File>() {
			override fun setupPickerMode(openPanel: ID) {
				Foundation.invoke(openPanel, "setCanChooseFiles:", false)
				Foundation.invoke(openPanel, "setCanChooseDirectories:", true)
			}

			override fun getResult(openPanel: ID): File? = singlePath(openPanel)
		}
	}
}
