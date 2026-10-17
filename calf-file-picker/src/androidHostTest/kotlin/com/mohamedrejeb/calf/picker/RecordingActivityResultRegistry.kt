package com.mohamedrejeb.calf.picker

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider

/** Records launches instead of starting activities, so the test can deliver the result itself. */
internal class RecordingActivityResultRegistry : ActivityResultRegistry() {
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
        if (!hasDocumentsApp) throw ActivityNotFoundException("No activity for the documents intent")
        launches += Launch(requestCode, contract, input)
    }

    @Suppress("UNCHECKED_CAST")
    fun lastIntent(): Intent {
        val launch = launches.last()
        val contract = launch.contract as ActivityResultContract<Any?, *>
        return contract.createIntent(ApplicationProvider.getApplicationContext(), launch.input)
    }

    fun dispatchLast(result: Any?) {
        dispatchResult(launches.last().requestCode, result)
    }
}
