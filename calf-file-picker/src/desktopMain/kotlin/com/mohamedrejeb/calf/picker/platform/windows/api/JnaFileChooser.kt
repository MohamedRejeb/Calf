package com.mohamedrejeb.calf.picker.platform.windows.api

import com.sun.jna.Platform
import java.awt.Window
import java.io.File
import java.util.Arrays
import java.util.Collections
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * JnaFileChooser is a wrapper around the native Windows file chooser
 * and folder browser that falls back to the Swing JFileChooser on platforms
 * other than Windows or if the user chooses a combination of features
 * that are not supported by the native dialogs (for example multiple
 * selection of directories).
 *
 * Example:
 * JnaFileChooser fc = new JnaFileChooser();
 * fc.setFilter("All Files", "*");
 * fc.setFilter("Pictures", "jpg", "jpeg", "gif", "png", "bmp");
 * fc.setMultiSelectionEnabled(true);
 * fc.setMode(JnaFileChooser.Mode.FilesAndDirectories);
 * if (fc.showOpenDialog(parent)) {
 * Files[] selected = fc.getSelectedFiles();
 * // do something with selected
 * }
 *
 * @see JFileChooser, WindowsFileChooser, WindowsFileBrowser
 */
internal class JnaFileChooser() {
	private enum class Action {
		Open, Save
	}

	/**
	 * the availabe selection modes of the dialog
	 */
	enum class Mode(val jFileChooserValue: Int) {
		Files(JFileChooser.FILES_ONLY),
		Directories(JFileChooser.DIRECTORIES_ONLY),
		FilesAndDirectories(JFileChooser.FILES_AND_DIRECTORIES)
	}

	var selectedFiles: Array<File?>
		protected set
	var currentDirectory: File? = null
		protected set
	protected var filters: ArrayList<Array<String>> = ArrayList()

	/**
	 * sets whether to enable multiselection
	 *
	 * @param enabled true to enable multiselection, false to disable it
	 */
	var isMultiSelectionEnabled: Boolean = false

	/**
	 * sets the selection mode
	 *
	 * @param mode the selection mode
	 */
	var mode: Mode = Mode.Files

	private var defaultFile: String = ""
	private var dialogTitle: String = ""
	private var openButtonText: String = ""
	private var saveButtonText: String = ""

	/**
	 * creates a new file chooser with multiselection disabled and mode set
	 * to allow file selection only.
	 */
	init {
		selectedFiles = arrayOf(null)
	}

	/**
	 * creates a new file chooser with the specified initial directory
	 *
	 * @param currentDirectory the initial directory
	 */
	constructor(currentDirectory: File?) : this() {
		if (currentDirectory != null) {
			this.currentDirectory =
				if (currentDirectory.isDirectory) currentDirectory else currentDirectory.parentFile
		}
	}

	/**
	 * creates a new file chooser with the specified initial directory
	 *
	 * @param currentDirectory the initial directory
	 */
	constructor(currentDirectoryPath: String?) : this(
		if (currentDirectoryPath != null) File(
			currentDirectoryPath
		) else null
	)

	/**
	 * shows a dialog for opening files
	 *
	 * @param parent the parent window
	 *
	 * @return true if the user clicked OK
	 */
	fun showOpenDialog(parent: Window?): Boolean {
		return showDialog(parent, Action.Open)
	}

	/**
	 * shows a dialog for saving files
	 *
	 * @param parent the parent window
	 *
	 * @return true if the user clicked OK
	 */
	fun showSaveDialog(parent: Window): Boolean {
		return showDialog(parent, Action.Save)
	}

	private fun showDialog(parent: Window?, action: Action): Boolean {
		// native windows filechooser doesn't support mixed selection mode
		if (Platform.isWindows() && mode != Mode.FilesAndDirectories) {
			// windows filechooser can only multiselect files
			if (isMultiSelectionEnabled && mode == Mode.Files) {
				// TODO Here we would use the native windows dialog
				// to choose multiple files. However I haven't been able
				// to get it to work properly yet because it requires
				// tricky callback magic and somehow this didn't work for me
				// quite as documented (probably because I messed something up).
				// Because I don't need this feature right now I've put it on
				// hold to get on with stuff.
				// Example code: http://support.microsoft.com/kb/131462/en-us
				// GetOpenFileName: http://msdn.microsoft.com/en-us/library/ms646927.aspx
				// OFNHookProc: http://msdn.microsoft.com/en-us/library/ms646931.aspx
				// CDN_SELCHANGE: http://msdn.microsoft.com/en-us/library/ms646865.aspx
				// SendMessage: http://msdn.microsoft.com/en-us/library/ms644950.aspx
			} else if (!isMultiSelectionEnabled) {
				if (mode == Mode.Files) {
					return showWindowsFileChooser(parent, action)
				} else if (mode == Mode.Directories) {
					return showWindowsFolderBrowser(parent)
				}
			}
		}

		// fallback to Swing
		return showSwingFileChooser(parent, action)
	}

	private fun showSwingFileChooser(parent: Window?, action: Action): Boolean {
		val fc = JFileChooser(currentDirectory)
		fc.isMultiSelectionEnabled = isMultiSelectionEnabled
		fc.fileSelectionMode = mode.jFileChooserValue

		// set select file
		if (defaultFile.isNotEmpty() and (action == Action.Save)) {
			val fsel = File(defaultFile)
			fc.selectedFile = fsel
		}
		if (dialogTitle.isNotEmpty()) {
			fc.dialogTitle = dialogTitle
		}
		if ((action == Action.Open) and openButtonText.isNotEmpty()) {
			fc.approveButtonText = openButtonText
		} else if ((action == Action.Save) and saveButtonText.isNotEmpty()) {
			fc.approveButtonText = saveButtonText
		}

		// build filters
		if (filters.size > 0) {
			var useAcceptAllFilter = false
			for (spec in filters) {
				// the "All Files" filter is handled specially by JFileChooser
				if (spec[1] == "*") {
					useAcceptAllFilter = true
					continue
				}
				fc.addChoosableFileFilter(
					FileNameExtensionFilter(
						spec[0], *Arrays.copyOfRange(spec, 1, spec.size)
					)
				)
			}
			fc.isAcceptAllFileFilterUsed = useAcceptAllFilter
		}

		var result = -1
		result = if (action == Action.Open) {
			fc.showOpenDialog(parent)
		} else {
			if (saveButtonText.isEmpty()) {
				fc.showSaveDialog(parent)
			} else {
				fc.showDialog(parent, null)
			}
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFiles =
				if (isMultiSelectionEnabled) fc.selectedFiles else arrayOf(fc.selectedFile)
			currentDirectory = fc.currentDirectory
			return true
		}

		return false
	}

	private fun showWindowsFileChooser(parent: Window?, action: Action): Boolean {
		val fc = WindowsFileChooser(currentDirectory)
		fc.setFilters(filters)

		if (defaultFile.isNotEmpty()) fc.setDefaultFilename(defaultFile)

		if (dialogTitle.isNotEmpty()) {
			fc.setTitle(dialogTitle)
		}

		val result = fc.showDialog(parent, action == Action.Open)
		if (result) {
			selectedFiles = arrayOf(fc.selectedFile)
			currentDirectory = fc.currentDirectory
		}
		return result
	}

	private fun showWindowsFolderBrowser(parent: Window?): Boolean {
		val fb = WindowsFolderBrowser()
		if (!dialogTitle.isEmpty()) {
			fb.setTitle(dialogTitle)
		}
		val file = fb.showDialog(parent)
		if (file != null) {
			selectedFiles = arrayOf(file)
			currentDirectory = if (file.parentFile != null) file.parentFile else file
			return true
		}

		return false
	}

	/**
	 * add a filter to the user-selectable list of file filters
	 *
	 * @param name   name of the filter
	 * @param filter you must pass at least 1 argument, the arguments are the file
	 * extensions.
	 */
	fun addFilter(name: String, vararg filter: String) {
		require(filter.isNotEmpty())
		val parts = ArrayList<String>()
		parts.add(name)
		Collections.addAll(parts, *filter)
		filters.add(parts.toTypedArray<String>())
	}

	fun setCurrentDirectory(currentDirectoryPath: String?) {
		this.currentDirectory =
			(if (currentDirectoryPath != null) File(currentDirectoryPath) else null)
	}

	fun setDefaultFileName(dfile: String) {
		this.defaultFile = dfile
	}

	/**
	 * set a title name
	 *
	 * @param Title of dialog
	 */
	fun setTitle(title: String) {
		this.dialogTitle = title
	}

	/**
	 * set a open button name
	 *
	 * @param open button text
	 */
	fun setOpenButtonText(buttonText: String) {
		this.openButtonText = buttonText
	}

	/**
	 * set a saveFile button name
	 *
	 * @param saveFile button text
	 */
	fun setSaveButtonText(buttonText: String) {
		this.saveButtonText = buttonText
	}

	val selectedFile: File?
		get() = selectedFiles[0]
}
