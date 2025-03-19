package com.pathfinder.khushu

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.pathfinder.khushu.databinding.ActivityMainBinding
import com.pathfinder.khushu.utils.DndSettings
import com.pathfinder.khushu.utils.GeofenceBroadcastReceiver
import com.pathfinder.khushu.utils.GeofenceService
import com.pathfinder.khushu.utils.NotificationHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val permissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_NOTIFICATION_POLICY,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionsIfNeeded()
        checkNotificationSettings()
        checkGpsIsSupported()
        checkGooglePlayServices(this)

        NotificationHelper.createNotificationChannel(this)
        DndSettings(this).requestDndPermission()

        // Start the service
        val intent = Intent(this, GeofenceService::class.java)
        startForegroundService(intent)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: throw IllegalStateException("Activity does not have a NavHostFragment")

        val navController: NavController = navHostFragment.navController
        val bottomNavigationView = binding.navView

        bottomNavigationView.setupWithNavController(navController)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_compass,
                R.id.navigation_maps
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun requestPermissionsIfNeeded() {
        var missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if(missingPermissions.contains(Manifest.permission.FOREGROUND_SERVICE_LOCATION)){
            if (Build.VERSION.SDK_INT < 34) {
                missingPermissions = missingPermissions.filter { it != Manifest.permission.FOREGROUND_SERVICE_LOCATION }
            }
        }

        if (missingPermissions.isNotEmpty()) {
            Log.d("Permissions", "Requesting permissions: $missingPermissions")
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            Log.d("Permissions", "All permissions already granted")
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach { (permission, granted) ->
                Log.d("Permissions", "$permission granted: $granted")
            }

            if (results.all { it.value }) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
               Toast.makeText(this, "Not all permissions granted", Toast.LENGTH_LONG).show()
            }
        }

    private fun checkNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (!notificationManager.areNotificationsEnabled()) {
                Log.e("Permissions", "Notifications are disabled! Prompting user to enable them.")
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            }
        }
    }

    private fun checkGpsIsSupported() {
        val isSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        if (!isSupported) {
            Toast.makeText(this, "Geofencing not supported on this device", Toast.LENGTH_LONG).show()
        }
    }



    private fun checkGooglePlayServices(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)

        if (resultCode == ConnectionResult.SUCCESS) {
            Log.d("GooglePlayServices", "Google Play Services is available")
            return true
        }
        Log.e(
            "GooglePlayServices",
            "Google Play Services is missing or needs an update. Error code: $resultCode"
        )
        return false
    }


}