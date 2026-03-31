package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext

internal actual fun resolveContext(): PlatformContext = AndroidContextHolder.context
