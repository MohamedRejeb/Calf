package com.mohamedrejeb.calf.picker.platform.windows.win32

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.WString

internal object Comdlg32 {
	init {
		Native.register("comdlg32")
	}

	external fun GetOpenFileNameW(params: OpenFileName?): Boolean
	external fun GetSaveFileNameW(params: OpenFileName?): Boolean
	external fun CommDlgExtendedError(): Int

	// flags for the OpenFileName structure
	const val OFN_READONLY: Int = 0x00000001
	const val OFN_OVERWRITEPROMPT: Int = 0x00000002
	const val OFN_HIDEREADONLY: Int = 0x00000004
	const val OFN_NOCHANGEDIR: Int = 0x00000008
	const val OFN_SHOWHELP: Int = 0x00000010
	const val OFN_ENABLEHOOK: Int = 0x00000020
	const val OFN_ENABLETEMPLATE: Int = 0x00000040
	const val OFN_ENABLETEMPLATEHANDLE: Int = 0x00000080
	const val OFN_NOVALIDATE: Int = 0x00000100
	const val OFN_ALLOWMULTISELECT: Int = 0x00000200
	const val OFN_EXTENSIONDIFFERENT: Int = 0x00000400
	const val OFN_PATHMUSTEXIST: Int = 0x00000800
	const val OFN_FILEMUSTEXIST: Int = 0x00001000
	const val OFN_CREATEPROMPT: Int = 0x00002000
	const val OFN_SHAREAWARE: Int = 0x00004000
	const val OFN_NOREADONLYRETURN: Int = 0x00008000
	const val OFN_NOTESTFILECREATE: Int = 0x00010000
	const val OFN_NONETWORKBUTTON: Int = 0x00020000
	const val OFN_NOLONGNAMES: Int = 0x00040000
	const val OFN_EXPLORER: Int = 0x00080000
	const val OFN_NODEREFERENCELINKS: Int = 0x00100000
	const val OFN_LONGNAMES: Int = 0x00200000
	const val OFN_ENABLEINCLUDENOTIFY: Int = 0x00400000
	const val OFN_ENABLESIZING: Int = 0x00800000
	const val OFN_DONTADDTORECENT: Int = 0x02000000
	const val OFN_FORCESHOWHIDDEN: Int = 0x10000000

	// error codes from cderr.h which may be returned by
	// CommDlgExtendedError for the GetOpenFileName and
	// GetSaveFileName functions.
	const val CDERR_DIALOGFAILURE: Int = 0xFFFF
	const val CDERR_FINDRESFAILURE: Int = 0x0006
	const val CDERR_INITIALIZATION: Int = 0x0002
	const val CDERR_LOADRESFAILURE: Int = 0x0007
	const val CDERR_LOADSTRFAILURE: Int = 0x0005
	const val CDERR_LOCKRESFAILURE: Int = 0x0008
	const val CDERR_MEMALLOCFAILURE: Int = 0x0009
	const val CDERR_MEMLOCKFAILURE: Int = 0x000A
	const val CDERR_NOHINSTANCE: Int = 0x0004
	const val CDERR_NOHOOK: Int = 0x000B
	const val CDERR_NOTEMPLATE: Int = 0x0003
	const val CDERR_STRUCTSIZE: Int = 0x0001
	const val FNERR_SUBCLASSFAILURE: Int = 0x3001
	const val FNERR_INVALIDFILENAME: Int = 0x3002
	const val FNERR_BUFFERTOOSMALL: Int = 0x3003

	class OpenFileName : Structure() {
		@JvmField var lStructSize: Int = size()
		@JvmField var hwndOwner: Pointer? = null
		@JvmField var hInstance: Pointer? = null
		@JvmField var lpstrFilter: WString? = null
		@JvmField var lpstrCustomFilter: WString? = null
		@JvmField var nMaxCustFilter: Int = 0
		@JvmField var nFilterIndex: Int = 0
		@JvmField var lpstrFile: Pointer? = null
		@JvmField var nMaxFile: Int = 0
		@JvmField var lpstrDialogTitle: String? = null
		@JvmField var nMaxDialogTitle: Int = 0
		@JvmField var lpstrInitialDir: WString? = null
		@JvmField var lpstrTitle: WString? = null
		@JvmField var Flags: Int = 0
		@JvmField var nFileOffset: Short = 0
		@JvmField var nFileExtension: Short = 0
		@JvmField var lpstrDefExt: String? = null
		@JvmField var lCustData: Pointer? = null
		@JvmField var lpfnHook: Pointer? = null
		@JvmField var lpTemplateName: Pointer? = null

		override fun getFieldOrder(): List<String> {
			return listOf(
				"lStructSize",
				"hwndOwner",
				"hInstance",
				"lpstrFilter",
				"lpstrCustomFilter",
				"nMaxCustFilter",
				"nFilterIndex",
				"lpstrFile",
				"nMaxFile",
				"lpstrDialogTitle",
				"nMaxDialogTitle",
				"lpstrInitialDir",
				"lpstrTitle",
				"Flags",
				"nFileOffset",
				"nFileExtension",
				"lpstrDefExt",
				"lCustData",
				"lpfnHook",
				"lpTemplateName"
			)
		}
	}
}
