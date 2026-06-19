package xy.onlasdan.galery.upload

import android.content.Context
import androidx.work.*
import ca.pkay.rcloneexplorer.util.FLog
import xy.onlasdan.galery.data.db.GalleryDatabase
import java.util.concurrent.TimeUnit

/**
 * Schedules the upload worker. Called from MainActivity or boot receiver.
 */
object UploadScheduler {

    private val TAG = "UploadScheduler"
    private const val WORK_NAME = "gallery_upload"

    /**
     * Schedule upload worker with network constraints.
     * @param context Application context
     * @param wifiOnly If true, only upload on Wi-Fi. If false, upload on any network.
     */
    fun schedule(context: Context, wifiOnly: Boolean = true) {
        val networkType = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED

        val request = PeriodicWorkRequestBuilder<UploadWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(networkType)
                    .build(),
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)

        FLog.d(TAG, "Upload scheduled (wifiOnly=$wifiOnly)")
    }

    /**
     * Schedule immediate one-time upload for a specific photo.
     */
    fun scheduleImmediate(context: Context) {
        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context)
            .enqueue(request)
    }

    /**
     * Cancel all pending uploads.
     */
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Check if there are pending uploads.
     */
    suspend fun hasPendingUploads(context: Context): Boolean {
        val db = GalleryDatabase.getInstance(context)
        return db.photoDao().pending().isNotEmpty()
    }
}
