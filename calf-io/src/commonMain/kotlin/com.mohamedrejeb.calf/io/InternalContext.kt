package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext

/**
 * Resolves the platform context internally.
 * On Android, returns the context from [AndroidContextHolder].
 * On all other platforms, returns [PlatformContext.INSTANCE].
 */
internal expect fun resolveContext(): PlatformContext
