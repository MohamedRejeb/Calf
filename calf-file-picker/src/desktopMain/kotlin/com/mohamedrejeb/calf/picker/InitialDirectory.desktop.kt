package com.mohamedrejeb.calf.picker

import com.mohamedrejeb.calf.io.KmpFile

/** The native dialogs open at a path. */
internal actual fun KmpFile.toInitialDirectory(): String? = file.path
