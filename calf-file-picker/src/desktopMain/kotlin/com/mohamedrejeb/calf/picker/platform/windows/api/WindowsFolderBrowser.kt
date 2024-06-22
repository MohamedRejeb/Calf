package com.mohamedrejeb.calf.picker.platform.windows.api

import com.mohamedrejeb.calf.picker.platform.windows.win32.Ole32.CoTaskMemFree
import com.mohamedrejeb.calf.picker.platform.windows.win32.Ole32.OleInitialize
import com.mohamedrejeb.calf.picker.platform.windows.win32.Shell32
import com.mohamedrejeb.calf.picker.platform.windows.win32.Shell32.SHBrowseForFolder
import com.mohamedrejeb.calf.picker.platform.windows.win32.Shell32.SHGetPathFromIDListW
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.awt.Window
import java.io.File

/**
 * The native Windows folder browser.
 *
 * Example:
 * WindowsFolderBrowser fb = new WindowsFolderBrowser();
 * File dir = fb.showDialog(parentWindow);
 * if (dir != null) {
 * // do something with dir
 * }
 */
internal class WindowsFolderBrowser {
	private var title: String?

	/**
	 * creates a new folder browser
	 */
	constructor() {
		title = null
	}

	/**
	 * creates a new folder browser with text that can be used as title
	 * or to give instructions to the user
	 *
	 * @param title text that will be displayed at the top of the dialog
	 */
	constructor(title: String?) {
		this.title = title
	}

	fun setTitle(title: String?) {
		this.title = title
	}

	/**
	 * displays the dialog to the user
	 *
	 * @param parent the parent window
	 *
	 * @return the selected directory or null if the user canceled the dialog
	 */
	fun showDialog(parent: Window?): File? {
		OleInitialize(null)
		val params = Shell32.BrowseInfo()
		params.hwndOwner = if (parent == null) null else Native.getWindowPointer(parent)
		params.ulFlags =  // disable the OK button if the user selects a virtual PIDL
			Shell32.BIF_RETURNONLYFSDIRS or  // BIF_USENEWUI is only available as of Windows 2000/Me (Shell32.dll 5.0)
				// but I guess no one is using older versions anymore anyway right?!
				// I don't know what happens if this is executed where it's
				// not supported.
				Shell32.BIF_USENEWUI
		if (title != null) {
			params.lpszTitle = title
		}
		val pidl = SHBrowseForFolder(params)
		if (pidl != null) {
			// MAX_PATH is 260 on Windows XP x32 so 4kB should
			// be more than big enough
			val path: Pointer = Memory((1024 * 4).toLong())
			SHGetPathFromIDListW(pidl, path)
			val filePath = path.getWideString(0)
			val file = File(filePath)
			CoTaskMemFree(pidl)
			return file
		}
		return null
	}
}
