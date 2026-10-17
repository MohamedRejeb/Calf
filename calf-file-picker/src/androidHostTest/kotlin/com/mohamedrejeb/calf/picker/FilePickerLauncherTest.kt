package com.mohamedrejeb.calf.picker

import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import com.mohamedrejeb.calf.io.KmpFile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Devices can have the documents app disabled; picking must then fail without crashing. */
@RunWith(RobolectricTestRunner::class)
class FilePickerLauncherTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val registry = RecordingActivityResultRegistry()
    private var result: List<KmpFile>? = null
    private var error: FilePickerException? = null
    private lateinit var launcher: FilePickerLauncher

    @Test
    fun withOnErrorADeviceWithoutADocumentsAppIsAnError() {
        registry.hasDocumentsApp = false
        setContent(type = FilePickerFileType.Pdf, withOnError = true)

        composeRule.runOnIdle { launcher.launch() }
        composeRule.waitForIdle()

        assertNotNull(error)
        assertNull(result)
    }

    @Test
    fun withOnErrorTheFolderPickerReportsAMissingDocumentsApp() {
        registry.hasDocumentsApp = false
        setContent(type = FilePickerFileType.Folder, withOnError = true)

        composeRule.runOnIdle { launcher.launch() }
        composeRule.waitForIdle()

        assertNotNull(error)
    }

    @Test
    fun withoutOnErrorAMissingDocumentsAppDoesNotCrash() {
        registry.hasDocumentsApp = false
        setContent(type = FilePickerFileType.Pdf, withOnError = false)

        composeRule.runOnIdle { launcher.launch() }
        composeRule.waitForIdle()

        assertNull(result)
    }

    @Test
    fun aPickedFileIsAResultNotAnError() {
        setContent(type = FilePickerFileType.Pdf, withOnError = true)
        val picked = Uri.parse("content://com.android.providers.downloads.documents/document/42")

        composeRule.runOnIdle { launcher.launch() }
        composeRule.runOnIdle { registry.dispatchLast(picked) }
        composeRule.waitForIdle()

        assertEquals(listOf(picked), result?.map { it.uri })
        assertNull(error)
    }

    private fun setContent(type: FilePickerFileType, withOnError: Boolean) {
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry: ActivityResultRegistry = registry
        }
        composeRule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                val onResult: (List<KmpFile>) -> Unit = { files -> result = files }
                launcher = if (withOnError) {
                    rememberFilePickerLauncher(type = type, onError = { error = it }, onResult = onResult)
                } else {
                    rememberFilePickerLauncher(type = type, onResult = onResult)
                }
            }
        }
    }
}
