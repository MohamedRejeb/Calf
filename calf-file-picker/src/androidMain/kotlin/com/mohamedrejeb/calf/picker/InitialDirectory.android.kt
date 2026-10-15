package com.mohamedrejeb.calf.picker

import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.toUriString

/** The folder picker opens at a URI, and a picked folder is a `content:` tree URI with no path. */
internal actual fun KmpFile.toInitialDirectory(): String? = toUriString()
