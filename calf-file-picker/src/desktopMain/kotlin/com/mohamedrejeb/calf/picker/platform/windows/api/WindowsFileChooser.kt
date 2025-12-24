package com.mohamedrejeb.calf.picker.platform.windows.api

import com.mohamedrejeb.calf.picker.platform.windows.win32.Comdlg32
import com.mohamedrejeb.calf.picker.platform.windows.win32.Comdlg32.CommDlgExtendedError
import com.mohamedrejeb.calf.picker.platform.windows.win32.Comdlg32.GetOpenFileNameW
import com.mohamedrejeb.calf.picker.platform.windows.win32.Comdlg32.GetSaveFileNameW
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.WString
import java.awt.Window
import java.io.File
import java.util.Collections

/**
 * The native Windows file chooser dialog.
 *
 * Example:
 * WindowsFileChooser fc = new WindowsFileChooser("C:\\");
 * fc.addFilter("All Files", "*");
 * fc.addFilter("Text files", "txt", "log", "xml", "css", "html");
 * fc.addFilter("Source code", "java", "c", "cpp", "cc", "h", "hpp");
 * fc.addFilter("Binary files", "exe", "class", "jar", "dll", "so");
 * if (fc.showOpenDialog(parent)) {
 * File f = fc.getSelectedFile();
 * // do something with f
 * }
 *
 * Note that although you can set the initial directory Windows will
 * determine the initial directory according to the following rules
 * (the initial directory is referred to as "lpstrInitialDir"):
 *
 * Windows 7:
 * 1. If lpstrInitialDir has the same value as was passed the first time the
 * application used an Open or Save As dialog box, the path most recently
 * selected by the user is used as the initial directory.
 * 2. Otherwise, if lpstrFile contains a path, that path is the initial
 * directory.
 * 3. Otherwise, if lpstrInitialDir is not NULL, it specifies the initial
 * directory.
 * 4. If lpstrInitialDir is NULL and the current directory contains any files of
 * the specified filter types, the initial directory is the current
 * directory.
 * 5. Otherwise, the initial directory is the personal files directory of the
 * current user.
 * 6. Otherwise, the initial directory is the Desktop folder.
 *
 * Windows 2000/XP/Vista:
 * 1. If lpstrFile contains a path, that path is the initial directory.
 * 2. Otherwise, lpstrInitialDir specifies the initial directory.
 * 3. Otherwise, if the application has used an Open or Save As dialog box in
 * the past, the path most recently used is selected as the initial
 * directory. However, if an application is not run for a long time, its
 * saved selected path is discarded.
 * 4. If lpstrInitialDir is NULL and the current directory contains any files
 * of the specified filter types, the initial directory is the current
 * directory.
 * 5. Otherwise, the initial directory is the personal files directory of the
 * current user.
 * 6. Otherwise, the initial directory is the Desktop folder.
 *
 * Therefore you probably want to use an exe wrapper like WinRun4J in order
 * for this to work properly on Windows 7. Otherwise multiple programs may
 * interfere with each other. Unfortunately there doesn't seem to be a way
 * to override this behaviour.
 *
 * [://msdn.microsoft.com/en-us/library/ms646839.aspx][http]
 * [://winrun4j.sourceforge.net/][http]
 */
internal class WindowsFileChooser {
    /**
     * returns the files selected by the user
     *
     * @return the selected files; empty array if the dialog was canceled or never shown
     */
	var selectedFiles: Array<File> = emptyArray()
		protected set

    /**
     * sets whether to enable multiselection
     *
     * @param enabled true to enable multiselection, false to disable it
     */
	var isMultiSelectionEnabled: Boolean = false

	/**
	 * returns the current directory
	 *
	 * This is always the parent directory of the chosen file, even if you
	 * enter an absolute path to a file that doesn't exist in the current
	 * directory.
	 *
	 * @return the current directory
	 */
	var currentDirectory: File? = null
		protected set
	private var filters: ArrayList<Array<String>>

	private var defaultFilename: String = ""
	private var dialogTitle: String = ""

	/**
	 * creates a new file chooser
	 */
	constructor() {
		filters = ArrayList()
	}

	/**
	 * creates a new file chooser with the specified initial directory
	 *
	 * If the given file is not a directory the parent file will be used instead.
	 *
	 * @param currentDirectory the initial directory
	 */
	constructor(currentDirectory: File?) {
		filters = ArrayList()
		if (currentDirectory != null) {
			this.currentDirectory =
				if (currentDirectory.isDirectory) currentDirectory else currentDirectory.parentFile
		}
	}

	/**
	 * creates a new file chooser with the specified initial directory path
	 *
	 * @param currentDirectoryPath the initial directory path; may be null
	 */
	constructor(currentDirectoryPath: String?) : this(
		if (currentDirectoryPath != null) File(
			currentDirectoryPath
		) else null
	)

	// this is a package private method used by the JnaFileChooser
	// facade to directly set the filter list
	fun setFilters(filters: ArrayList<Array<String>>) {
		this.filters = filters
	}

	/**
	 * add a filter to the user-selectable list of file filters
	 *
	 * @param name name of the filter
	 * @param filter you must pass at least 1 argument, the arguments
	 * are the file extensions.
	 */
	fun addFilter(name: String, vararg filter: String) {
		require(filter.size >= 1)
		val parts = mutableListOf<String>()
		parts.add(name)
		Collections.addAll(parts, *filter)
		filters.add(parts.toTypedArray<String>())
	}

	/**
	 * set a title name
	 *
	 * @param Title of dialog
	 */
	fun setTitle(tname: String) {
		this.dialogTitle = tname
	}

	/**
	 * show the dialog for opening a file
	 *
	 * @param parent the parent window of the dialog
	 *
	 * @return true if the user clicked ok, false otherwise
	 */
	fun showOpenDialog(parent: Window?): Boolean {
		return showDialog(parent, true)
	}

	/**
	 * show the dialog for saving a file
	 *
	 * @param parent the parent window of the dialog
	 *
	 * @return true if the user clicked ok, false otherwise
	 */
	fun showSaveDialog(parent: Window?): Boolean {
		return showDialog(parent, false)
	}

	/*
	 * shows the dialog
	 *
	 * @param parent the parent window
	 * @param open whether to show the open dialog, if false saveFile dialog is shown
	 *
	 * @return true if the user clicked ok, false otherwise
	 */
	fun showDialog(parent: Window?, open: Boolean): Boolean {
		val params = Comdlg32.OpenFileName()
		params.Flags =  // use explorer-style interface
			(Comdlg32.OFN_EXPLORER // the dialog changes the current directory when browsing,
				// this flag causes the original value to be restored after the
				// dialog returns
				or Comdlg32.OFN_NOCHANGEDIR // disable "open as read-only" feature
				or Comdlg32.OFN_HIDEREADONLY // enable resizing of the dialog
				or Comdlg32.OFN_ENABLESIZING)

		if (isMultiSelectionEnabled) {
			params.Flags = params.Flags or Comdlg32.OFN_ALLOWMULTISELECT
		}

		params.hwndOwner = if (parent == null) null else Native.getWindowPointer(parent)

		// lpstrFile contains the selection path after the dialog
		// returns. It must be big enough for the path to fit or
		// GetOpenFileName returns an error (FNERR_BUFFERTOOSMALL).
		// MAX_PATH is 260 so 4*260+1 bytes should be big enough (I hope...)
		// http://msdn.microsoft.com/en-us/library/aa365247.aspx#maxpath
		val bufferLength = if (isMultiSelectionEnabled) 32 * 1024 else 260
		// 4 bytes per char + 1 null byte
		val bufferSize = 4 * bufferLength + 1
		params.lpstrFile = Memory(bufferSize.toLong())
		if (defaultFilename.isNotEmpty()) {
			params.lpstrFile?.setWideString(0, defaultFilename)
		} else {
			params.lpstrFile?.clear(bufferSize.toLong())
		}
		if (!dialogTitle.isEmpty()) {
			params.lpstrTitle = WString(dialogTitle)
		}

		// nMaxFile
		// http://msdn.microsoft.com/en-us/library/ms646839.aspx:
		// "The size, in characters, of the buffer pointed to by
		// lpstrFile. The buffer must be large enough to store the
		// path and file name string or strings, including the
		// terminating NULL character."

		// Therefore because we're using the unicode version of the
		// API the nMaxFile value must be 1/4 of the lpstrFile
		// buffer size plus one for the terminating null byte.
		params.nMaxFile = bufferLength

		if (currentDirectory != null) {
			params.lpstrInitialDir = WString(currentDirectory!!.absolutePath)
		}

		// build filter string if filters were specified
		if (filters.size > 0) {
			params.lpstrFilter = WString(buildFilterString())
			params.nFilterIndex = 1 // TODO don't hardcode here
		}

		val approved = if (open) GetOpenFileNameW(params) else GetSaveFileNameW(params)

		if (approved) {
			val ptr = params.lpstrFile!!
			if (isMultiSelectionEnabled) {
				val strings = ArrayList<String>()
				var offset = 0L
				while (true) {
					val str = ptr.getWideString(offset)
					if (str.isEmpty()) break
					strings.add(str)
					offset += (str.length + 1) * 2
				}

				if (strings.size == 1) {
					val f = File(strings[0])
					selectedFiles = arrayOf(f)
					currentDirectory = f.parentFile
				} else {
					val dir = File(strings[0])
					currentDirectory = dir
					selectedFiles = strings.drop(1).map { File(dir, it) }.toTypedArray()
				}
			} else {
				val filePath = ptr.getWideString(0)
				val file = File(filePath)
				selectedFiles = arrayOf(file)
				currentDirectory = file.parentFile
			}
		} else {
			val errCode = CommDlgExtendedError()
			// if the code is 0 the user clicked cancel
			if (errCode != 0) {
				throw RuntimeException(
					"GetOpenFileName failed with error $errCode"
				)
			}
		}
		return approved
	}

	/*
	 * builds a filter string
	 *
	 * from MSDN:
	 * A buffer containing pairs of null-terminated filter strings. The last
	 * string in the buffer must be terminated by two NULL characters.
	 *
	 * The first string in each pair is a display string that describes the
	 * filter (for example, "Text Files"), and the second string specifies the
	 * filter pattern (for example, "*.TXT"). To specify multiple filter
	 * patterns for a single display string, use a semicolon to separate the
	 * patterns (for example, "*.TXT;*.DOC;*.BAK").
	 *
	 * http://msdn.microsoft.com/en-us/library/ms646839.aspx
	 */
	private fun buildFilterString(): String {
		val filterStr = StringBuilder()
		for (spec in filters) {
			val label = spec[0]
			// add label and terminate with null byte
			filterStr.append(label)
			filterStr.append('\u0000')
			// build file extension patterns seperated by a
			// semicolon and terminated by a null byte
			for (i in 1 until spec.size) {
				filterStr.append("*.")
				filterStr.append(spec[i])
				filterStr.append(';')
			}
			// remove last superfluous ";" and add terminator
			filterStr.deleteCharAt(filterStr.length - 1)
			filterStr.append('\u0000')
		}
		// final terminator
		filterStr.append('\u0000')
		return filterStr.toString()
	}

	fun setDefaultFilename(defaultFilename: String) {
		this.defaultFilename = defaultFilename
	}
}
