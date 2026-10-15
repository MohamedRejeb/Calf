package com.mohamedrejeb.calf.picker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The system save sheet pauses and resumes the activity, so the composition that receives the
 * result is not the one that launched the save. The source given to `launch` must survive that.
 */
@OptIn(ExperimentalCalfApi::class)
@RunWith(RobolectricTestRunner::class)
class FileSaverLauncherTest {
    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val registry = RecordingActivityResultRegistry()
    private val recompositions = mutableIntStateOf(0)
    private var result: SaverResult? = null
    private lateinit var launcher: FileSaverLauncher

    @Test
    fun bytesAreWrittenWhenTheResultArrivesInALaterComposition() {
        setContent()
        val destination = tempFolder.newFile("Report.pdf")
        val bytes = byteArrayOf(1, 2, 3, 4)

        composeRule.runOnIdle { launcher.launch(bytes = bytes, baseName = "Report", extension = "pdf") }
        recompose()
        deliver(Uri.fromFile(destination))

        assertContentEquals(bytes, destination.readBytes())
        assertEquals(Uri.fromFile(destination), result?.file?.uri)
    }

    @Test
    fun aFileIsCopiedWhenTheResultArrivesInALaterComposition() {
        setContent()
        val source = tempFolder.newFile("source.pdf").apply { writeBytes(byteArrayOf(9, 8, 7)) }
        val destination = tempFolder.newFile("Copy.pdf")

        composeRule.runOnIdle {
            launcher.launch(file = KmpFile(Uri.fromFile(source)), baseName = "Copy", extension = "pdf")
        }
        recompose()
        deliver(Uri.fromFile(destination))

        assertContentEquals(byteArrayOf(9, 8, 7), destination.readBytes())
        assertTrue(source.exists(), "the source is left in place")
    }

    @Test
    fun aSourceThatCannotBeReadReportsNull() {
        setContent()
        val missing = KmpFile(Uri.fromFile(tempFolder.root.resolve("missing.pdf")))
        val destination = tempFolder.newFile("Copy.pdf")

        composeRule.runOnIdle { launcher.launch(file = missing, baseName = "Copy", extension = "pdf") }
        deliver(Uri.fromFile(destination))

        assertNull(result?.file)
    }

    @Test
    fun cancellingReportsNull() {
        setContent()

        composeRule.runOnIdle { launcher.launch(bytes = byteArrayOf(1), baseName = "Report", extension = "pdf") }
        recompose()
        deliver(null)

        assertNull(result?.file)
    }

    @Test
    fun theDocumentIsCreatedWithTheRequestedNameAndTheExtensionsMimeType() {
        setContent()

        composeRule.runOnIdle { launcher.launch(bytes = byteArrayOf(1), baseName = "Report", extension = "pdf") }
        val intent = composeRule.runOnIdle { registry.lastIntent() }

        assertEquals(Intent.ACTION_CREATE_DOCUMENT, intent.action)
        assertEquals("Report.pdf", intent.getStringExtra(Intent.EXTRA_TITLE))
        assertEquals("application/pdf", intent.type)
    }

    @Test
    fun anUnknownExtensionIsCreatedAsAnOctetStream() {
        setContent()

        composeRule.runOnIdle { launcher.launch(bytes = byteArrayOf(1), baseName = "Data", extension = "calfunknown") }
        val intent = composeRule.runOnIdle { registry.lastIntent() }

        assertEquals("Data.calfunknown", intent.getStringExtra(Intent.EXTRA_TITLE))
        assertEquals("application/octet-stream", intent.type)
    }

    @Test
    fun aDeviceWithoutADocumentsAppReportsNullInsteadOfCrashing() {
        registry.hasDocumentsApp = false
        setContent()

        composeRule.runOnIdle { launcher.launch(bytes = byteArrayOf(1), baseName = "Report", extension = "pdf") }
        composeRule.waitUntil(timeoutMillis = 5_000) { result != null }

        assertNull(result?.file)
    }

    private fun setContent() {
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry: ActivityResultRegistry = registry
        }
        composeRule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                // Reading the counter here, and capturing it in onResult, makes every bump re-run
                // rememberFileSaverLauncher, like the resume after the system sheet does.
                val generation = recompositions.intValue
                launcher = rememberFileSaverLauncher { file ->
                    result = SaverResult(file, generation)
                }
            }
        }
    }

    private fun recompose() {
        composeRule.runOnIdle { recompositions.intValue++ }
        composeRule.waitForIdle()
    }

    private fun deliver(uri: Uri?) {
        composeRule.runOnIdle { registry.dispatchLast(uri) }
        composeRule.waitUntil(timeoutMillis = 5_000) { result != null }
    }

    private class SaverResult(val file: KmpFile?, val generation: Int)
}

/** Records launches instead of starting activities, so the test can deliver the result itself. */
private class RecordingActivityResultRegistry : ActivityResultRegistry() {
    private class Launch(val requestCode: Int, val contract: ActivityResultContract<*, *>, val input: Any?)

    private val launches = mutableListOf<Launch>()

    /** When false, launches fail the way they do on devices with the documents app disabled. */
    var hasDocumentsApp = true

    override fun <I, O> onLaunch(
        requestCode: Int,
        contract: ActivityResultContract<I, O>,
        input: I,
        options: ActivityOptionsCompat?,
    ) {
        if (!hasDocumentsApp) throw ActivityNotFoundException("No activity for ACTION_CREATE_DOCUMENT")
        launches += Launch(requestCode, contract, input)
    }

    @Suppress("UNCHECKED_CAST")
    fun lastIntent(): Intent {
        val launch = launches.last()
        val contract = launch.contract as ActivityResultContract<Any?, *>
        return contract.createIntent(ApplicationProvider.getApplicationContext(), launch.input)
    }

    fun dispatchLast(uri: Uri?) {
        dispatchResult(launches.last().requestCode, uri)
    }
}
