package com.mohamedrejeb.calf.ui.web

import org.w3c.dom.Window

internal actual fun Window.evaluateScript(script: String): Any? =
    asDynamic().eval(script)
