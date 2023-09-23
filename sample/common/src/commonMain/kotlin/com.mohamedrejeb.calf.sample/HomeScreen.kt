package com.mohamedrejeb.calf.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    navigate: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Text(
            text = "Calf",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(16.dp)
        )

        val colorScheme = MaterialTheme.colorScheme

        Text(
            text = buildAnnotatedString {
                val spanStyle = SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.primary)
                withStyle(spanStyle) {
                    append("C")
                }
                append("ompose ")
                withStyle(spanStyle) {
                    append("A")
                }
                append("daptive ")
                withStyle(spanStyle) {
                    append("L")
                }
                append("ook & ")
                withStyle(spanStyle) {
                    append("F")
                }
                append("eel")
            },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(16.dp)
        )
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ListItem(
                    onClick = { navigate(Screen.Dialog.name) },
                    title = "Adaptive Alert Dialog",
                    icon = Icons.Outlined.Warning,
                )
                ListItem(
                    onClick = { navigate(Screen.BottomSheet.name) },
                    title = "Adaptive Bottom Sheet",
                    icon = Icons.Outlined.Pages,
                )
                ListItem(
                    onClick = { navigate(Screen.ProgressBar.name) },
                    title = "Adaptive Progress Bar",
                    icon = Icons.Outlined.Refresh,
                )
                ListItem(
                    onClick = { navigate(Screen.Switch.name) },
                    title = "Adaptive Switch",
                    icon = Icons.Outlined.ToggleOn,
                )
//                ListItem(
//                    onClick = { navigate(Screen.DropDownMenu.name) },
//                    title = "Adaptive Drop Down Menu",
//                    icon = Icons.Outlined.Menu,
//                )
                ListItem(
                    onClick = { navigate(Screen.DatePicker.name) },
                    title = "Adaptive Date Picker",
                    icon = Icons.Outlined.CalendarMonth,
                )
                ListItem(
                    onClick = { navigate(Screen.TimePicker.name) },
                    title = "Adaptive Time Picker",
                    icon = Icons.Outlined.PunchClock,
                )
                ListItem(
                    onClick = { navigate(Screen.FilePicker.name) },
                    title = "Adaptive File Picker",
                    icon = Icons.Outlined.AttachFile,
                )
                ListItem(
                    onClick = { navigate(Screen.WebView.name) },
                    title = "Adaptive Web View",
                    icon = Icons.Outlined.Web,
                )
//                ListItem(
//                    onClick = {  },
//                    title = "Adaptive Notification",
//                    icon = Icons.Outlined.Notifications,
//                )
//                ListItem(
//                    onClick = {  },
//                    title = "Adaptive Permission",
//                    icon = Icons.Outlined.PermIdentity,
//                )
            }
        }
    }
}

@Composable
fun ListItem(
    onClick: () -> Unit,
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(.2f)
            )
            .clickable {
                onClick()
            }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            Icons.Outlined.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}