package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.camerapicker.rememberCameraPickerLauncher
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.toUriString
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.sample.Platform
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.EmptyState
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.components.displayName
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.ui.button.AdaptiveButton

private const val USAGE = """
val camera = rememberCameraPickerLauncher { photo ->
    avatar = photo
}
val permission = rememberPermissionState(Permission.Camera) { granted ->
    if (granted) camera.launch()
}

AdaptiveButton(onClick = {
    if (permission.status.isGranted) camera.launch()
    else permission.launchPermissionRequest()
}) { Text("Take photo") }
"""

private const val PHOTO_ASPECT_RATIO = 4f / 3f

/** The launcher has a camera to open on Android and iOS only; elsewhere `launch()` does nothing. */
private val hasCameraSupport: Boolean
    get() = currentPlatform == Platform.Android || currentPlatform == Platform.IOS

@Immutable
private data class CapturedPhoto(
    val file: KmpFile,
    val name: String,
    val path: String,
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPickerScreen(navigateBack: () -> Unit) {
    val context = LocalPlatformContext.current
    var photo by remember { mutableStateOf<CapturedPhoto?>(null) }

    val cameraLauncher = rememberCameraPickerLauncher(
        onResult = { file ->
            photo = CapturedPhoto(
                file = file,
                name = file.getName(context).orEmpty(),
                path = file.getPath(context) ?: file.toUriString().orEmpty(),
            )
        },
    )
    val cameraPermission = rememberPermissionState(
        permission = Permission.Camera,
        onPermissionResult = { granted -> if (granted) cameraLauncher.launch() },
    )

    SampleScreenScaffold(title = Screen.CameraPickerScreen.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.CameraPickerScreen)
            PhotoSection(
                photo = photo,
                onTakePhoto = {
                    if (cameraPermission.status.isGranted) {
                        cameraLauncher.launch()
                    } else {
                        cameraPermission.launchPermissionRequest()
                    }
                },
            )
            CameraAccessSection(status = cameraPermission.status)
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun PhotoSection(
    photo: CapturedPhoto?,
    onTakePhoto: () -> Unit,
) {
    DemoSection(title = "Photo") {
        DemoCard {
            when {
                photo != null -> CapturedPhotoPreview(photo)
                hasCameraSupport -> EmptyState(
                    icon = Icons.Outlined.PhotoCamera,
                    title = "No photo yet",
                    message = "Take a photo. It shows up here with its file name and path.",
                )
                else -> EmptyState(
                    icon = Icons.Outlined.NoPhotography,
                    title = "No camera on ${currentPlatform.displayName}",
                    message = "The camera picker opens the system camera on Android and iOS. " +
                        "Here, use the image picker instead.",
                )
            }
        }
        AdaptiveButton(
            onClick = onTakePhoto,
            enabled = hasCameraSupport,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (photo == null) "Take photo" else "Take another photo")
        }
    }
}

@Composable
private fun CapturedPhotoPreview(photo: CapturedPhoto) {
    AsyncImage(
        model = photo.file,
        contentDescription = "Captured photo",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(PHOTO_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    )
    ValueRow(label = "Name", value = photo.name.ifEmpty { "Unknown" })
    ValueRow(label = "Path", value = photo.path.ifEmpty { "Unknown" })
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraAccessSection(status: PermissionStatus) {
    DemoSection(
        title = "Camera access",
        footer = "The demo asks for the camera permission first, then opens the camera. " +
            "The iOS simulator has no camera, so it opens the photo library instead.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Camera",
                subtitle = "Permission.Camera",
                leading = { IconTile(icon = Icons.Outlined.PhotoCamera, color = TileIndigo) },
                trailing = { PermissionStatusLabel(status) },
            )
        }
    }
}
