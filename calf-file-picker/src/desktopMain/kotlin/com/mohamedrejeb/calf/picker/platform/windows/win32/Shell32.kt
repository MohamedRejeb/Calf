package com.mohamedrejeb.calf.picker.platform.windows.win32

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure

internal object Shell32 {
	init {
		Native.register("shell32")
	}

	external fun SHBrowseForFolder(params: BrowseInfo?): Pointer?
	external fun SHGetPathFromIDListW(pidl: Pointer?, path: Pointer?): Boolean

	// flags for the BrowseInfo structure
	const val BIF_RETURNONLYFSDIRS: Int = 0x00000001
	const val BIF_DONTGOBELOWDOMAIN: Int = 0x00000002
	const val BIF_NEWDIALOGSTYLE: Int = 0x00000040
	const val BIF_EDITBOX: Int = 0x00000010
	const val BIF_USENEWUI: Int = BIF_EDITBOX or BIF_NEWDIALOGSTYLE
	const val BIF_NONEWFOLDERBUTTON: Int = 0x00000200
	const val BIF_BROWSEINCLUDEFILES: Int = 0x00004000
	const val BIF_SHAREABLE: Int = 0x00008000
	const val BIF_BROWSEFILEJUNCTIONS: Int = 0x00010000

	// http://msdn.microsoft.com/en-us/library/bb773205.aspx
	class BrowseInfo : Structure() {
		@JvmField var hwndOwner: Pointer? = null
		@JvmField var pidlRoot: Pointer? = null
		@JvmField var pszDisplayName: String? = null
		@JvmField var lpszTitle: String? = null
		@JvmField var ulFlags: Int = 0
		@JvmField var lpfn: Pointer? = null
		@JvmField var lParam: Pointer? = null
		@JvmField var iImage: Int = 0

		override fun getFieldOrder(): List<String> {
			return listOf(
				"hwndOwner",
				"pidlRoot",
				"pszDisplayName",
				"lpszTitle",
				"ulFlags",
				"lpfn",
				"lParam",
				"iImage"
			)
		}
	}
}
