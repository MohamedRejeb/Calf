package com.mohamedrejeb.calf.sample.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Pages
import androidx.compose.material.icons.outlined.PunchClock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.ViewCompact
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material.icons.outlined.WebAsset
import androidx.compose.ui.graphics.vector.ImageVector

enum class ScreenCategory(val title: String) {
    UIComponents("UI Components"),
    Pickers("Pickers"),
    PlatformAPIs("Platform APIs"),
    Navigation("Navigation"),
}

enum class Screen(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: ScreenCategory,
) {
    Home(
        title = "Home",
        description = "",
        icon = Icons.Outlined.ViewCompact,
        category = ScreenCategory.UIComponents,
    ),
    Dialog(
        title = "Adaptive Alert Dialog",
        description = "Native alert dialogs with iOS UIAlertController and Material3 AlertDialog",
        icon = Icons.Outlined.Warning,
        category = ScreenCategory.UIComponents,
    ),
    BottomSheet(
        title = "Adaptive Bottom Sheet",
        description = "Modal bottom sheets with native iOS presentation",
        icon = Icons.Outlined.Pages,
        category = ScreenCategory.UIComponents,
    ),
    ProgressBar(
        title = "Adaptive Progress Bar",
        description = "Circular progress indicators with Cupertino and Material styles",
        icon = Icons.Outlined.Refresh,
        category = ScreenCategory.UIComponents,
    ),
    Switch(
        title = "Adaptive Switch",
        description = "Toggle switches with native iOS UISwitch and Material3 Switch",
        icon = Icons.Outlined.ToggleOn,
        category = ScreenCategory.UIComponents,
    ),
    Button(
        title = "Adaptive Buttons",
        description = "Buttons with Cupertino style on iOS and Material3 on other platforms",
        icon = Icons.Outlined.CheckBox,
        category = ScreenCategory.UIComponents,
    ),
    AdaptiveClickable(
        title = "Adaptive Clickable",
        description = "Platform-aware click feedback with ripple and highlight effects",
        icon = Icons.Outlined.AdsClick,
        category = ScreenCategory.UIComponents,
    ),
    DropDown(
        title = "Adaptive Drop Down",
        description = "Context menus with iOS UIMenu and Material3 DropdownMenu",
        icon = Icons.Outlined.Menu,
        category = ScreenCategory.UIComponents,
    ),
    DatePicker(
        title = "Adaptive Date Picker",
        description = "Date selection with native iOS UIDatePicker and Material3 DatePicker",
        icon = Icons.Outlined.CalendarMonth,
        category = ScreenCategory.Pickers,
    ),
    TimePicker(
        title = "Adaptive Time Picker",
        description = "Time selection with native iOS UIDatePicker and Material3 TimePicker",
        icon = Icons.Outlined.PunchClock,
        category = ScreenCategory.Pickers,
    ),
    FilePicker(
        title = "Adaptive File Picker",
        description = "File selection with platform-native file pickers",
        icon = Icons.Outlined.AttachFile,
        category = ScreenCategory.Pickers,
    ),
    ImagePicker(
        title = "Adaptive Image Picker",
        description = "Image selection with native photo library pickers",
        icon = Icons.Outlined.Image,
        category = ScreenCategory.Pickers,
    ),
    CameraPickerScreen(
        title = "Camera Picker",
        description = "Camera capture with native platform camera APIs",
        icon = Icons.Outlined.Camera,
        category = ScreenCategory.Pickers,
    ),
    WebView(
        title = "Adaptive Web View",
        description = "Embedded web content with WKWebView on iOS and WebView on Android",
        icon = Icons.Outlined.Web,
        category = ScreenCategory.PlatformAPIs,
    ),
    Permission(
        title = "Permissions",
        description = "Runtime permission handling across platforms",
        icon = Icons.Outlined.Security,
        category = ScreenCategory.PlatformAPIs,
    ),
    Map(
        title = "Adaptive Map",
        description = "Map views with Apple Maps on iOS and Google Maps on Android",
        icon = Icons.Outlined.Map,
        category = ScreenCategory.PlatformAPIs,
    ),
    NavigationBar(
        title = "Adaptive Navigation Bar",
        description = "Bottom navigation with iOS UITabBar and Material3 NavigationBar",
        icon = Icons.Outlined.Navigation,
        category = ScreenCategory.Navigation,
    ),
    ScaffoldDemo(
        title = "Adaptive Scaffold",
        description = "Combined Scaffold with AdaptiveTopBar and AdaptiveNavigationBar",
        icon = Icons.Outlined.ViewCompact,
        category = ScreenCategory.Navigation,
    ),
    TopBarDemo(
        title = "Adaptive Top Bar",
        description = "Top app bar with native iOS UINavigationBar and Material3 TopAppBar",
        icon = Icons.Outlined.WebAsset,
        category = ScreenCategory.Navigation,
    ),
    ExpandableFAB(
        title = "Adaptive Expandable FAB",
        description = "Expandable floating action button with Liquid Glass on iOS and Material3 animation",
        icon = Icons.Outlined.Add,
        category = ScreenCategory.UIComponents,
    ),
    Toolbar(
        title = "Adaptive Toolbar",
        description = "Floating toolbar with Liquid Glass on iOS and Material3 Expressive HorizontalFloatingToolbar",
        icon = Icons.Outlined.Menu,
        category = ScreenCategory.UIComponents,
    ),
}
