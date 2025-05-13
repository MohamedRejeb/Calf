package com.mohamedrejeb.calf.camera_picker

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.mohamedrejeb.calf.io.KmpFile
import java.io.File


class CameraPickerLauncherImp(
    private val context: Context,
    private val launcher: androidx.activity.result.ActivityResultLauncher<Uri>,
    private val onResult: (KmpFile) -> Unit
) : CameraPickerLauncher {

    private var imageUri by mutableStateOf<Uri?>(null)

    override fun launch() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return // لا يوجد كاميرا على الجهاز
        }

        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = if (android.os.Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } else {
            Uri.fromFile(file)
        }

        imageUri = uri
        launcher.launch(uri)
    }

    fun handleResult(success: Boolean) {
        if (success && imageUri != null) {
            onResult(KmpFile(imageUri!!))
        }
    }
}

@Composable
actual fun rememberCameraPickerLauncher(
    onResult: (KmpFile) -> Unit,
): CameraPickerLauncher {
    val context = LocalContext.current

    val cameraPickerLauncher = remember {
        mutableStateOf<CameraPickerLauncherImp?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        cameraPickerLauncher.value?.handleResult(success)
    }

    return remember {
        CameraPickerLauncherImp(context, launcher, onResult).also {
            cameraPickerLauncher.value = it
        }
    }
}