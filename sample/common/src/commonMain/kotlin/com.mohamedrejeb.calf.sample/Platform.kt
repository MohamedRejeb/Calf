package com.mohamedrejeb.calf.sample

enum class Platform {
    Android,
    IOS,
    Desktop,
    Web;

    val isAndroid: Boolean
        get() = this == Android

    val isIOS: Boolean

        get() = this == IOS

    val isDesktop: Boolean
        get() = this == Desktop

    val isWeb: Boolean
        get() = this == Web
}

expect val currentPlatform: Platform