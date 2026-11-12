package com.mohamedrejeb.calf.sample.navigation

import com.mohamedrejeb.calf.sample.Platform

/**
 * What draws a demo on [platform]: the native control or system API behind the Calf call. Shown in
 * each demo's header, so a recording of the same screen on two platforms says what is native.
 * `null` when the demo is not about one component.
 */
fun Screen.nativeImplementation(platform: Platform, isLiquidGlass: Boolean): String? {
    val ios = platform == Platform.IOS
    return when (this) {
        Screen.Home, Screen.Showcase -> null
        Screen.AdaptiveNavigation ->
            if (ios) "UITabBarController + UINavigationController" else "Navigation 3 + navigation suite"
        Screen.Foldable -> when (platform) {
            Platform.IOS -> "UIWindowScene hinge and regions"
            Platform.Android -> "Jetpack WindowManager"
            Platform.Desktop, Platform.Web -> "No fold on this platform"
        }
        Screen.Button -> if (ios) iosStyle(isLiquidGlass, "UIButton") else "Material 3 Button"
        Screen.Switch -> if (ios) "UISwitch" else "Material 3 Switch"
        Screen.Slider -> if (ios) iosStyle(isLiquidGlass, "Cupertino slider") else "Material 3 Slider"
        Screen.ProgressBar -> if (ios) "UIActivityIndicatorView" else "Material 3 CircularProgressIndicator"
        Screen.AdaptiveClickable -> if (ios) "iOS press-to-scale feedback" else "Material ripple"
        Screen.Toolbar -> if (ios) iosStyle(isLiquidGlass, "Cupertino toolbar") else "Material 3 Expressive toolbar"
        Screen.CupertinoIconsCatalog -> "SF Symbols style font, bundled"
        Screen.Dialog -> if (ios) "UIAlertController" else "Material 3 AlertDialog"
        Screen.BottomSheet -> if (ios) "UISheetPresentationController" else "Material 3 ModalBottomSheet"
        Screen.DropDown -> if (ios) "UIMenu" else "Material 3 DropdownMenu"
        Screen.DatePicker -> if (ios) "UIDatePicker" else "Material 3 DatePicker"
        Screen.TimePicker -> if (ios) "UIDatePicker" else "Material 3 TimePicker"
        Screen.FilePicker, Screen.ImagePicker -> filePicker(platform)
        Screen.CameraPickerScreen -> when (platform) {
            Platform.IOS -> "UIImagePickerController"
            Platform.Android -> "ActivityResultContracts.TakePicture"
            Platform.Desktop, Platform.Web -> "Not available on this platform"
        }
        Screen.ProfilePicture -> when (platform) {
            Platform.IOS -> "UIDocumentPickerViewController"
            Platform.Android -> "ActivityResultContracts.CreateDocument"
            Platform.Desktop -> "Native save dialog"
            Platform.Web -> "Browser save dialog or download"
        }
        Screen.Nav3ListDetail -> "Navigation 3 NavDisplay"
        Screen.NavigationBar -> if (ios) "UITabBar" else "Material 3 NavigationBar"
        Screen.ScaffoldDemo -> if (ios) "UINavigationBar + UITabBar" else "Material 3 Scaffold"
        Screen.TopBarDemo -> if (ios) "UINavigationBar" else "Material 3 TopAppBar"
        Screen.Share -> when (platform) {
            Platform.IOS -> "UIActivityViewController"
            Platform.Android -> "Android share sheet"
            Platform.Desktop -> "Native share sheet"
            Platform.Web -> "Web Share API"
        }
        Screen.WebView -> when (platform) {
            Platform.IOS -> "WKWebView"
            Platform.Android -> "Android WebView"
            Platform.Desktop -> "JavaFX WebView"
            Platform.Web -> "iframe"
        }
        Screen.Permission -> when (platform) {
            Platform.IOS -> "iOS authorization APIs"
            Platform.Android -> "Android runtime permissions"
            Platform.Desktop, Platform.Web -> "Not supported on this platform"
        }
    }
}

private fun iosStyle(isLiquidGlass: Boolean, fallback: String): String =
    if (isLiquidGlass) "Liquid Glass" else fallback

private fun filePicker(platform: Platform): String =
    when (platform) {
        Platform.IOS -> "PHPickerViewController / UIDocumentPicker"
        Platform.Android -> "Activity Result API"
        Platform.Desktop -> "Native file dialog"
        Platform.Web -> "<input type=file>"
    }
