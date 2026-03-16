package com.mohamedrejeb.calf.ui.web

import org.w3c.dom.Window

/**
 * Evaluates a JavaScript expression in the given window context.
 * This requires an expect/actual because the eval API differs between JS and WasmJS.
 */
internal expect fun Window.evaluateScript(script: String): Any?
