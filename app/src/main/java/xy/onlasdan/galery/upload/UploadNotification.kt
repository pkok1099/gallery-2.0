package xy.onlasdan.galery.upload

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import ca.pkay.rcloneexplorer.R

/**
 * Handles notifications for upload status.
 */
object UploadNotification {

    private const val CHANNEL_ID = "galery_upload_channel"
    private const val CHANNEL_NAME = "Upload Status"
    private const val NOTIFICATION_ID = 1001

    /**
     * Create notification channel for Android O+.
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows upload progress and status"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show upload progress notification.
     */
    fun showProgress(context: Context, current: Int, total: Int) {
        createChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_upload)
            .setContentTitle("Uploading photos")
            .setContentText("$current of $total photos")
            .setProgress(total, current, false)
            .setOngoing(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Show upload complete notification.
     */
    fun showComplete(context: Context, count: Int) {
        createChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_upload)
            .setContentTitle("Upload complete")
            .setContentText("$count photos uploaded")
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    /**
     * Show upload error notification.
     */
    fun showError(context: Context, message: String) {
        createChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_upload)
            .setContentTitle("Upload failed")
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }

    /**
     * Cancel all notifications.
     */
    fun cancelAll(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
    }
}
