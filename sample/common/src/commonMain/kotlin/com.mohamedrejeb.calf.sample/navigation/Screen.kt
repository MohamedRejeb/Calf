package com.mohamedrejeb.calf.sample.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImportContacts
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pages
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SmartButton
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.WebAsset
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.ui.graphics.vector.ImageVector

/** The sections of the home list, in display order. */
enum class ScreenCategory(val title: String) {
    StartHere("Start here"),
    Components("Components"),
    Presentation("Dialogs, sheets and menus"),
    Pickers("Pickers"),
    Navigation("Navigation"),
    PlatformAPIs("Platform APIs"),
}

/**
 * Every demo of the sample, in display order within its [category].
 *
 * @property api the Calf API the demo is about, shown in its header.
 */
enum class Screen(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: ScreenCategory,
    val api: String,
) {
    Home(
        title = "Calf",
        description = "Compose Adaptive Look & Feel",
        icon = Icons.Outlined.Dashboard,
        category = ScreenCategory.StartHere,
        api = "",
    ),

    // Start here
    Showcase(
        title = "Showcase",
        description = "A complete screen built only from adaptive components",
        icon = Icons.Outlined.AutoAwesome,
        category = ScreenCategory.StartHere,
        api = "calf-ui",
    ),
    AdaptiveNavigation(
        title = "Adaptive Navigation",
        description = "Native tabs, stacks and split view from one back stack",
        icon = Icons.Outlined.SwapHoriz,
        category = ScreenCategory.StartHere,
        api = "AdaptiveTabDisplay",
    ),
    Foldable(
        title = "Foldable",
        description = "Posture, fold, camera area and panes, drawn live",
        icon = Icons.Outlined.ImportContacts,
        category = ScreenCategory.StartHere,
        api = "rememberFoldState",
    ),

    // Components
    Button(
        title = "Buttons",
        description = "Filled, icon and Liquid Glass buttons",
        icon = Icons.Outlined.SmartButton,
        category = ScreenCategory.Components,
        api = "AdaptiveButton",
    ),
    Switch(
        title = "Switch",
        description = "UISwitch on iOS, Material 3 elsewhere",
        icon = Icons.Outlined.ToggleOn,
        category = ScreenCategory.Components,
        api = "AdaptiveSwitch",
    ),
    Slider(
        title = "Slider",
        description = "Liquid Glass on iOS 26, Material 3 elsewhere",
        icon = Icons.Outlined.Tune,
        category = ScreenCategory.Components,
        api = "AdaptiveSlider",
    ),
    ProgressBar(
        title = "Progress Indicator",
        description = "The iOS activity indicator and Material's spinner",
        icon = Icons.Outlined.Autorenew,
        category = ScreenCategory.Components,
        api = "AdaptiveCircularProgressIndicator",
    ),
    AdaptiveClickable(
        title = "Clickable",
        description = "Highlight on iOS, ripple on Material",
        icon = Icons.Outlined.AdsClick,
        category = ScreenCategory.Components,
        api = "Modifier.adaptiveClickable",
    ),
    Toolbar(
        title = "Floating Toolbar",
        description = "Liquid Glass toolbar and Material 3 Expressive",
        icon = Icons.Outlined.ViewDay,
        category = ScreenCategory.Components,
        api = "AdaptiveToolbar",
    ),
    CupertinoIconsCatalog(
        title = "Cupertino Icons",
        description = "1300+ SF Symbols style icons on every platform",
        icon = Icons.Outlined.Widgets,
        category = ScreenCategory.Components,
        api = "CupertinoIcons",
    ),

    // Dialogs, sheets and menus
    Dialog(
        title = "Alert Dialog",
        description = "UIAlertController on iOS, Material 3 elsewhere",
        icon = Icons.Outlined.Warning,
        category = ScreenCategory.Presentation,
        api = "AdaptiveAlertDialog",
    ),
    BottomSheet(
        title = "Bottom Sheet",
        description = "Native sheet presentation with detents on iOS",
        icon = Icons.Outlined.Pages,
        category = ScreenCategory.Presentation,
        api = "AdaptiveBottomSheet",
    ),
    DropDown(
        title = "Drop Down Menu",
        description = "UIMenu on iOS, Material 3 menu elsewhere",
        icon = Icons.Outlined.MoreHoriz,
        category = ScreenCategory.Presentation,
        api = "AdaptiveDropDown",
    ),

    // Pickers
    DatePicker(
        title = "Date Picker",
        description = "Inline, compact and range pickers",
        icon = Icons.Outlined.CalendarMonth,
        category = ScreenCategory.Pickers,
        api = "AdaptiveDatePicker",
    ),
    TimePicker(
        title = "Time Picker",
        description = "UIDatePicker on iOS, Material 3 elsewhere",
        icon = Icons.Outlined.Schedule,
        category = ScreenCategory.Pickers,
        api = "AdaptiveTimePicker",
    ),
    FilePicker(
        title = "File Picker",
        description = "The system file picker on every platform",
        icon = Icons.Outlined.AttachFile,
        category = ScreenCategory.Pickers,
        api = "rememberFilePickerLauncher",
    ),
    ImagePicker(
        title = "Image Picker",
        description = "Photos from the gallery, previewed with Coil",
        icon = Icons.Outlined.Image,
        category = ScreenCategory.Pickers,
        api = "FilePickerFileType.Image",
    ),
    CameraPickerScreen(
        title = "Camera",
        description = "Take a photo with the system camera",
        icon = Icons.Outlined.Camera,
        category = ScreenCategory.Pickers,
        api = "rememberCameraPickerLauncher",
    ),
    ProfilePicture(
        title = "File Saver",
        description = "Pick, edit and save a file back to disk",
        icon = Icons.Outlined.SaveAlt,
        category = ScreenCategory.Pickers,
        api = "rememberFileSaverLauncher",
    ),

    // Navigation
    Nav3ListDetail(
        title = "Navigation 3 List-Detail",
        description = "Plain NavDisplay with a fold-aware two-pane strategy",
        icon = Icons.Outlined.VerticalSplit,
        category = ScreenCategory.Navigation,
        api = "rememberCalfListDetailSceneStrategy",
    ),
    NavigationBar(
        title = "Navigation Bar",
        description = "UITabBar on iOS, Material 3 navigation bar",
        icon = Icons.Outlined.CropSquare,
        category = ScreenCategory.Navigation,
        api = "AdaptiveNavigationBar",
    ),
    ScaffoldDemo(
        title = "Scaffold",
        description = "Top bar, tab bar and content insets together",
        icon = Icons.Outlined.List,
        category = ScreenCategory.Navigation,
        api = "AdaptiveScaffold",
    ),
    TopBarDemo(
        title = "Top Bar",
        description = "UINavigationBar on iOS, Material 3 top app bar",
        icon = Icons.Outlined.WebAsset,
        category = ScreenCategory.Navigation,
        api = "AdaptiveTopBar",
    ),

    // Platform APIs
    Share(
        title = "Share",
        description = "Text, links and files through the share sheet",
        icon = Icons.Outlined.Share,
        category = ScreenCategory.PlatformAPIs,
        api = "rememberShareLauncher",
    ),
    WebView(
        title = "Web View",
        description = "WKWebView, Android WebView and more",
        icon = Icons.Outlined.Language,
        category = ScreenCategory.PlatformAPIs,
        api = "WebView",
    ),
    Permission(
        title = "Permissions",
        description = "Ask for and track runtime permissions",
        icon = Icons.Outlined.Security,
        category = ScreenCategory.PlatformAPIs,
        api = "rememberPermissionState",
    ),
}
