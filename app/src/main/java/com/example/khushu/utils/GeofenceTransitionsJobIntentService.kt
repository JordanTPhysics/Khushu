package com.example.khushu.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.khushu.utils.NotificationHelper
import com.example.khushu.utils.PreferencesRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceTransitionsJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        handleGeofenceEvent(applicationContext, intent)
    }

    @SuppressLint("MissingPermission")
    private fun handleGeofenceEvent(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            Log.e("GeofenceService", "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }

        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val preferencesRepository = PreferencesRepository(sharedPreferences)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationHelper = NotificationHelper(context)

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
                            "DND settings enabled."
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
                            "DND restored to ${preferencesRepository.previousDndMode}."
                        )
                    }
                }
                notificationManager.setInterruptionFilter(preferencesRepository.previousDndMode)
            }
        }
    }

    companion object {
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, 1001, intent)
        }
    }
}
