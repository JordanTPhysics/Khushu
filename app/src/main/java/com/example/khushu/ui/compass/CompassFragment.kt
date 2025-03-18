package com.example.khushu.ui.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.khushu.R
import com.example.khushu.databinding.FragmentCompassBinding
import com.example.khushu.utils.LocationService
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CompassFragment : Fragment(), SensorEventListener {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var _binding: FragmentCompassBinding? = null
    private val binding get() = _binding!!
    private lateinit var sensorManager: SensorManager
    private lateinit var locationService: LocationService
    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)
    private val makkahCoordinates = LatLng(21.4225, 39.8262)
    private var azimuth: Float = 0f
    private var qiblaAngle: Float? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val compassViewModel =
            ViewModelProvider(this).get(CompassViewModel::class.java)

        _binding = FragmentCompassBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)

        locationService = LocationService(requireContext())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.compass.post {
            val compassCenterX = binding.compass.x + binding.compass.width / 2
            val compassCenterY = binding.compass.y + binding.compass.height / 2

            binding.qiblaIcon.pivotX = compassCenterX - binding.qiblaIcon.x
            binding.qiblaIcon.pivotY = compassCenterY - binding.qiblaIcon.y
        }

        if (locationService.isPermissionGranted()) {
            locationService.getLastLocation { location: Location? ->
                location?.let {
                    val userPosition = LatLng(it.latitude, it.longitude)
                    qiblaAngle = userPosition.calculateBearing(makkahCoordinates)
                    updateCompass(azimuth, qiblaAngle)
                }
            }
        } else {
            locationService.requestPermission(requireActivity(), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            updateCompass(azimuth, qiblaAngle)
        }
    }

    private fun updateCompass(azimuth: Float, qiblaAngle: Float? = null) {
        val textView: TextView = binding.textCompass
        val compassNeedle: ImageView = binding.compass
        val qiblaNeedle: ImageView = binding.qiblaIcon

        val direction = getDirection(azimuth)
        textView.text = "$direction, ${String.format("%.1f", azimuth)}°"
        compassNeedle.rotation = -azimuth

        qiblaAngle?.let {
            qiblaNeedle.rotation = it - azimuth
            if (Math.abs(it - azimuth) < 2) {
                qiblaNeedle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purple_700))
            } else {
                qiblaNeedle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
            }

        }
    }

    private fun getDirection(azimuth: Float): String {
        return when {
            azimuth < 22.5 -> "N"
            azimuth < 67.5 -> "NE"
            azimuth < 112.5 -> "E"
            azimuth < 157.5 -> "SE"
            azimuth < 202.5 -> "S"
            azimuth < 247.5 -> "SW"
            azimuth < 292.5 -> "W"
            azimuth < 337.5 -> "NW"
            else -> "N"
        }
    }

    private fun LatLng.calculateBearing(destination: LatLng): Float {
        val lat1 = Math.toRadians(this.latitude)
        val lon1 = Math.toRadians(this.longitude)
        val lat2 = Math.toRadians(destination.latitude)
        val lon2 = Math.toRadians(destination.longitude)
        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360) % 360
        return bearing
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        sensorManager.unregisterListener(this)
    }
}