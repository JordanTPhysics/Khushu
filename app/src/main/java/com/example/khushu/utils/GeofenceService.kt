package com.example.khushu.utils
import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.khushu.MainActivity
import com.example.khushu.R
import com.example.khushu.lib.Place
import com.google.android.gms.location.*


class GeofenceService : LifecycleService() {

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var preferencesRepository: PreferencesRepository
    private val geofenceList = mutableListOf<Geofence>()
    private val _geofenceLiveData = MutableLiveData<List<Geofence>>()
    val geofenceLiveData: LiveData<List<Geofence>> get() = _geofenceLiveData
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        geofencingClient = LocationServices.getGeofencingClient(this)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        preferencesRepository = PreferencesRepository(sharedPreferences)

        startForeground(1, getNotification())

        // Observe LiveData and update geofences when places change
        preferencesRepository.getPlacesLiveData().observe(this,
            { places -> updateGeofences(places) })
    }

    inner class LocalBinder : Binder() {
        fun getService(): GeofenceService = this@GeofenceService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    fun requestPermission(context: Context, requestCode: Int) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateGeofences(places: List<Place>?) {
        if (places.isNullOrEmpty()) return


        geofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener {
            geofenceList.clear()
            places.forEach { place ->
                val geofence = Geofence.Builder()
                    .setRequestId(place.name)
                    .setCircularRegion(place.lat, place.lng, 50f)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
                geofenceList.add(geofence)
            }

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build()

            val notificationHelper = NotificationHelper(this)

            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener {
                    _geofenceLiveData.postValue(geofenceList)
                }
                .addOnFailureListener {
                    e -> notificationHelper.sendNotification(
                        "Geofence Error",
                        "Failed to add geofences, restart the app or contact support."
                    ) }
        }
    }

    fun getCurrentGeofences(): List<Geofence> {
        return geofenceList
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(this, 0, intent, flag)
    }

    private fun getNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, flag)

        return NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Geofence Service Running")
            .setContentText("Monitoring geofences for saved places.")
            .setColor(getColor(R.color.purple_500))
            .setSmallIcon(R.drawable.location_on_24dp)
            .setContentIntent(pendingIntent)
            .build()
    }
}
