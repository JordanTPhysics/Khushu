package com.example.khushu.utils

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class DndService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        enableDoNotDisturb()
        return START_STICKY
    }

    private fun enableDoNotDisturb() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
