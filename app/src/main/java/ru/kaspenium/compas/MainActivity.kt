package ru.kaspenium.compas

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import java.lang.Math.toDegrees

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var vDeegree: TextView
    private lateinit var image: ImageView
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private lateinit var manager: SensorManager
    var lastAccelerometer = FloatArray(3)
    var lastMagnetometer = FloatArray(3)
    var lastAccelerometerSet = false
    var lastMagnetometerSet = false
    var currentDegree: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        vDeegree = findViewById(R.id.degree)
        image = findViewById(R.id.arrow)
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
    }

    override fun onResume() {
        super.onResume()
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        manager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        manager.unregisterListener(this, accelerometer)
        manager.unregisterListener(this, magnetometer)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor === accelerometer) {
            lowPass(event.values, lastAccelerometer)
            lastAccelerometerSet = true
        } else if (event.sensor === magnetometer) {
            lowPass(event.values, lastMagnetometer)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val r = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val degree = (toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360

                val rotateAnimation = RotateAnimation(
                    currentDegree.toFloat(),
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f)
                rotateAnimation.duration = 1000
                rotateAnimation.fillAfter = true

                image.startAnimation(rotateAnimation)
                currentDegree = (-degree).toInt()
                vDeegree.text = (-currentDegree).toString()
            }
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.05f

        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}