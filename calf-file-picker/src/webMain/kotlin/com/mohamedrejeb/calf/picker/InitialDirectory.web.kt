package com.mohamedrejeb.calf.picker

import com.mohamedrejeb.calf.io.KmpFile

/** Browsers cannot open a file dialog in a given folder. */
internal actual fun KmpFile.toInitialDirectory(): String? = null
