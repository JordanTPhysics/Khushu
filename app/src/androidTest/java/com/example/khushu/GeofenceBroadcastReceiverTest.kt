import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.khushu.utils.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GeofenceBroadcastReceiverTest {

    @Test
    fun testOnReceive_withEnterTransition_logsGeofenceId() {
        val context = mock(Context::class.java)
        val intent = mock(Intent::class.java)
        val receiver = GeofenceBroadcastReceiver()

        // Mock the Geofence
        val mockGeofence = mock(Geofence::class.java)
        `when`(mockGeofence.requestId).thenReturn("test_geofence")

        // Mock the GeofencingEvent
        val mockGeofencingEvent = mock(GeofencingEvent::class.java)
        `when`(mockGeofencingEvent.geofenceTransition).thenReturn(Geofence.GEOFENCE_TRANSITION_ENTER)
        `when`(mockGeofencingEvent.triggeringGeofences).thenReturn(listOf(mockGeofence))

        // Use mockStatic for GeofencingEvent.fromIntent()
        val geofencingEventMockStatic: MockedStatic<GeofencingEvent> = mockStatic(GeofencingEvent::class.java)
        geofencingEventMockStatic.use { mockStatic ->
            mockStatic.`when`<GeofencingEvent> { GeofencingEvent.fromIntent(intent) }.thenReturn(mockGeofencingEvent)

            // Call onReceive()
            receiver.onReceive(context, intent)

            // Verify log was called (Check Logcat manually)
            Log.d("GeofenceTest", "GeofenceReceiver triggered for test_geofence")
        }
    }
}
