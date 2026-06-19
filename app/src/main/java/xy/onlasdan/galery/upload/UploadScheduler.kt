package xy.onlasdan.galery.upload

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Schedules the upload worker. Called from MainActivity or boot receiver.
 */
object UploadScheduler {

    private const val WORK_NAME = "gallery_upload"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<UploadWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
