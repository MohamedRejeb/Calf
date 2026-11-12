package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

private const val USAGE = """
var enabled by remember { mutableStateOf(true) }

AdaptiveSwitch(
    checked = enabled,
    onCheckedChange = { enabled = it },
)
"""

@Composable
fun SwitchScreen(navigateBack: () -> Unit) {
    SampleScreenScaffold(title = Screen.Switch.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.Switch)
            ConnectivitySection()
            StatesSection()
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun ConnectivitySection() {
    var airplaneMode by rememberSaveable { mutableStateOf(false) }
    var wifi by rememberSaveable { mutableStateOf(true) }
    var bluetooth by rememberSaveable { mutableStateOf(true) }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var darkMode by rememberSaveable { mutableStateOf(false) }

    DemoSection(
        title = "Settings",
        footer = "Airplane mode turns Wi-Fi and Bluetooth off and disables them, like the real thing.",
    ) {
        DemoListCard {
            SwitchRow(
                title = "Airplane Mode",
                icon = Icons.Outlined.AirplanemodeActive,
                tint = TileAmber,
                checked = airplaneMode,
                onCheckedChange = { airplaneMode = it },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            SwitchRow(
                title = "Wi-Fi",
                subtitle = if (wifi && !airplaneMode) "Home network" else "Off",
                icon = Icons.Outlined.Wifi,
                tint = TileBlue,
                checked = wifi && !airplaneMode,
                enabled = !airplaneMode,
                onCheckedChange = { wifi = it },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            SwitchRow(
                title = "Bluetooth",
                subtitle = if (bluetooth && !airplaneMode) "2 devices connected" else "Off",
                icon = Icons.Outlined.Bluetooth,
                tint = TileIndigo,
                checked = bluetooth && !airplaneMode,
                enabled = !airplaneMode,
                onCheckedChange = { bluetooth = it },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            SwitchRow(
                title = "Notifications",
                icon = Icons.Outlined.Notifications,
                tint = TileCoral,
                checked = notifications,
                onCheckedChange = { notifications = it },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            SwitchRow(
                title = "Dark Appearance",
                icon = Icons.Outlined.DarkMode,
                tint = TileViolet,
                checked = darkMode,
                onCheckedChange = { darkMode = it },
            )
        }
    }
}

@Composable
private fun StatesSection() {
    DemoSection(title = "States") {
        DemoListCard {
            DemoRow(
                title = "Disabled, off",
                trailing = { AdaptiveSwitch(checked = false, onCheckedChange = null, enabled = false) },
            )
            DemoDivider()
            DemoRow(
                title = "Disabled, on",
                trailing = { AdaptiveSwitch(checked = true, onCheckedChange = null, enabled = false) },
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    icon: ImageVector,
    tint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    DemoRow(
        title = title,
        subtitle = subtitle,
        leading = { IconTile(icon = icon, color = tint) },
        trailing = {
            AdaptiveSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        },
    )
}
