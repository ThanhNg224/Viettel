package com.example.viettel.stringee

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import java.lang.ref.WeakReference

class SensorManagerUtils private constructor(private val context: Context) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    @Suppress("DEPRECATION")
    private var keyguardLock: KeyguardManager.KeyguardLock? = null

    @SuppressLint("WakelockTimeout")
    fun acquireProximitySensor(tag: String) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        proximitySensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val screenLockValue = PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK
        wakeLock = powerManager?.newWakeLock(screenLockValue, tag)
        wakeLock?.acquire(10 * 60 * 1000L)  // 10 minutes timeout
    }

    @Suppress("unused")
    @SuppressLint("MissingPermission")
    fun disableKeyguard() {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        @Suppress("DEPRECATION")
        keyguardLock = keyguardManager?.newKeyguardLock(Context.KEYGUARD_SERVICE)
        @Suppress("DEPRECATION")
        keyguardLock?.disableKeyguard()
    }

    fun releaseSensor() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        proximitySensor = null

        wakeLock?.let { lock ->
            if (lock.isHeld) {
                lock.release()
            }
        }
        wakeLock = null

        @SuppressLint("MissingPermission")
        @Suppress("DEPRECATION")
        fun reenableKeyguard() {
            keyguardLock?.reenableKeyguard()
        }
        reenableKeyguard()
        keyguardLock = null

        instanceRef = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val value = event.values.firstOrNull() ?: return
        if (value == 0f) {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(10 * 60 * 1000L)  // 10 minutes timeout
            }
        } else {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        @Volatile
        private var instanceRef: WeakReference<SensorManagerUtils>? = null

        fun getInstance(context: Context): SensorManagerUtils =
            instanceRef?.get() ?: synchronized(this) {
                instanceRef?.get() ?: SensorManagerUtils(context.applicationContext).also {
                    instanceRef = WeakReference(it)
                }
            }

        @Suppress("unused")
        fun clearInstance() {
            instanceRef?.get()?.releaseSensor()
            instanceRef = null
        }
    }
}
