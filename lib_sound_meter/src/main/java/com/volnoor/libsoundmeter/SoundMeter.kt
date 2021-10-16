package com.volnoor.libsoundmeter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

const val DEFAULT_UPDATE_TIME_MILLIS = 50L

class SoundMeter(
    private val context: Context,
    private val configuration: Configuration
) : LifecycleObserver {

    constructor(context: Context) : this(context, Configuration(DEFAULT_UPDATE_TIME_MILLIS))

    private val lifecycleOwner: LifecycleOwner? = context as? LifecycleOwner?

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    private val audioSensor: AudioSensor = AudioSensor()
    private val listeners = mutableListOf<Listener>()
    private val handler = Handler(Looper.getMainLooper())

    private val updateCommand = Runnable { runUpdate() }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        stop()
    }

    @RequiresPermission(value = Manifest.permission.RECORD_AUDIO)
    fun start() {
        if (!checkPermission()) {
            throw RuntimeException("RECORD_AUDIO permission is not granted")
        }
        audioSensor.start()
        scheduleUpdate()
    }

    fun stop() {
        handler.removeCallbacks(updateCommand)
        audioSensor.stop()
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)) {
                return false
            }
        }
        return true
    }

    private fun scheduleUpdate() {
        handler.postDelayed(updateCommand, configuration.updateIntervalMs)
    }

    private fun runUpdate() {
        val amplitudeDb = audioSensor.amplitudeDb
        for (listener in listeners) {
            listener.onUpdateSoundLevel(amplitudeDb)
        }
        scheduleUpdate()
    }

    class Configuration(val updateIntervalMs: Long)

    fun interface Listener {
        fun onUpdateSoundLevel(soundLevelDb: Double)
    }
}