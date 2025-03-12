package com.example.khushu.utils

import android.Manifest
import com.example.khushu.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "channelId" // Use the same channel ID as in your notification
        private const val CHANNEL_NAME = "Geofence Alerts"
        private const val CHANNEL_DESC = "Notifications for geofence events"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26+
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // Adjust importance as needed
                ).apply {
                    description = CHANNEL_DESC
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID) // Use the same channel ID
            .setSmallIcon(R.drawable.location_on_24dp)
            .setColor(context.getColor(R.color.purple_500))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, builder.build()) // Unique ID for notification
    }
}


