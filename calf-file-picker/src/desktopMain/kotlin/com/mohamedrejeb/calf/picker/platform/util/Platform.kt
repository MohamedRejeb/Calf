package com.mohamedrejeb.calf.picker.platform.util

internal object PlatformUtil {
	val current: Platform
		get() {
			val system = System.getProperty("os.name").lowercase()
			return when {
				system.contains("win") ->
					Platform.Windows

				system.contains("nix") || system.contains("nux") || system.contains("aix") ->
					Platform.Linux

				system.contains("mac") ->
					Platform.MacOS

				else ->
					Platform.Linux
			}
		}
}

internal enum class Platform {
	Linux,
	MacOS,
	Windows
}
