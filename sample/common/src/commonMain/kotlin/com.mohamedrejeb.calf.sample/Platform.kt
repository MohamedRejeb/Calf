package com.mohamedrejeb.calf.sample

enum class Platform {
    Android,
    IOS,
    Desktop,
    Web
}

expect val currentPlatform: Platform