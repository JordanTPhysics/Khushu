package com.example.khushu.ui.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.khushu.databinding.FragmentCompassBinding
import kotlin.math.nextTowards
import kotlin.math.nextUp

class CompassFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentCompassBinding? = null
    private val binding get() = _binding!!
    private lateinit var sensorManager: SensorManager
    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val compassViewModel =
            ViewModelProvider(this).get(CompassViewModel::class.java)

        _binding = FragmentCompassBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textCompass
        val compassNeedle: ImageView = binding.compassNeedle

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)

        return root
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            updateCompass(azimuth)
        }
    }

    private fun calculateQiblaAngle(azimuth: Float): Float {
        val qiblaAngle = 39.826161f
        return qiblaAngle - azimuth
    }

    private fun updateCompass(azimuth: Float) {
        val textView: TextView = binding.textCompass
        val compassNeedle: ImageView = binding.compassNeedle
        val direction = getDirection(azimuth)
        textView.text = "$direction, ${azimuth.nextUp()}°"
        compassNeedle.rotation = -azimuth
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

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        sensorManager.unregisterListener(this)
    }
}