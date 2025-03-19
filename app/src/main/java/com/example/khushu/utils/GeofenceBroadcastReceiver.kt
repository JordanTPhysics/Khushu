package com.pathfinder.khushu.utils

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {

        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val preferencesRepository = PreferencesRepository(sharedPreferences)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationHelper = NotificationHelper(context)
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceService", "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }
        // Check if DND access is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            Log.e("GeofenceService", "DND access not granted. Cannot change DND settings.")
            return
        }

        val places = preferencesRepository.getPlacesSync() ?: return

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                preferencesRepository.saveDndMode(notificationManager.currentInterruptionFilter)
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)

                geofencingEvent.triggeringGeofences?.forEach { geofence ->
                    val place = places.find { it.name == geofence.requestId }
                    place?.let {
                        notificationHelper.sendNotification(
                            "Entering ${place.name}",
                            "Do Not Disturb activated."
                        )
                    }
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                geofencingEvent.triggeringGeofences?.forEach { geofence ->
                    val place = places.find { it.name == geofence.requestId }
                    place?.let {
                        notificationHelper.sendNotification(
                            "Leaving ${place.name}",
                            "Do Not Disturb setting restored."
                        )
                    }
                }
                notificationManager.setInterruptionFilter(preferencesRepository.getDndMode())
            }
        }
    }
}
