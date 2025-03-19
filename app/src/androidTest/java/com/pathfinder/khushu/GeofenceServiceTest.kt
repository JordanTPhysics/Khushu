package com.pathfinder.khushu

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.pathfinder.khushu.utils.GeofenceService

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class GeofenceServiceTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    @Test
    fun testServiceIsRunning() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val isRunning = manager.getRunningServices(Int.MAX_VALUE).any {
            it.service.className == GeofenceService::class.java.name
        }

        assertTrue("Service should be running", isRunning)
    }

    @Test
    fun testGeofenceService() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val serviceIntent = Intent(appContext, GeofenceService::class.java)

        // Start the service
        val binder = serviceRule.bindService(serviceIntent)
        assertNotNull("Service should start", binder)

        // Request permissions
        val geofenceService = (binder as GeofenceService.LocalBinder).getService()
        geofenceService.requestPermission(appContext, 2)

        // Stop the service
        serviceRule.unbindService()
    }
}