package com.mohamedrejeb.calf.ui.web

class WebSettings() {
    var javaScriptEnabled: Boolean = false

    val androidSettings = AndroidSettings()

    val iosSettings = IosSettings()

    val desktopSettings = DesktopSettings()

    class AndroidSettings {
        var supportZoom = true
        var textZoom = 100
        var allowContentAccess = true
        var allowFileAccess = true
        var blockNetworkImage = false
        var blockNetworkLoads = false
        var databaseEnabled = true
        var domStorageEnabled = true
        var loadsImagesAutomatically = true
        var useWideViewPort = true
        var loadWithOverviewMode = true
        var javaScriptCanOpenWindowsAutomatically = true
        var mediaPlaybackRequiresUserGesture = false
        var supportMultipleWindows = true
        var builtInZoomControls = true
        var displayZoomControls = false
        var setGeolocationEnabled = true
    }

    class IosSettings {

    }

    class DesktopSettings {

    }
}