package ca.pkay.rcloneexplorer.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

class NotificationUtils {

    companion object {

        @SuppressLint("MissingPermission")
        @JvmStatic
        fun createNotification(context: Context, notificationId: Int, notification: Notification) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, notification)
        }

        @JvmStatic
        fun createNotificationChannel(context: Context, channelId: String, channelName: String, importance: Int, description: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    importance
                )
                channel.description = description
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }
}
