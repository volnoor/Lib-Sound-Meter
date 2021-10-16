package com.volnoor.libsoundmeter.showcase

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.volnoor.libsoundmeter.SoundMeter
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var soundMeter: SoundMeter

    private lateinit var audioPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var btnStart: Button
    private lateinit var pbSoundLevel: ProgressBar
    private lateinit var tvSoundLevel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSoundMeter()

        audioPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                Log.d(TAG, "audio permission granted: $granted")
                if (granted) {
                    onAudioPermissionGranted()
                } else {
                    showToast("Record Audio permission must be granted")
                }
            }

        btnStart = findViewById(R.id.btn_start)
        pbSoundLevel = findViewById(R.id.pb_sound_level)
        tvSoundLevel = findViewById(R.id.tv_sound_level)
        btnStart.setOnClickListener { onButtonStartClicked() }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundMeter.stop()
    }

    private fun initSoundMeter() {
        soundMeter = SoundMeter(this)
        soundMeter.addListener {
            val soundLevelValue = it.roundToInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pbSoundLevel.setProgress(soundLevelValue, true)
            } else {
                pbSoundLevel.progress = soundLevelValue
            }
            tvSoundLevel.text = "$soundLevelValue dB"
        }
    }

    private fun onButtonStartClicked() {
        Log.d(TAG, "onButtonStartClicked")

        btnStart.visibility = View.GONE
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    @SuppressLint("MissingPermission")
    private fun onAudioPermissionGranted() {
        Log.d(TAG, "onAudioPermissionGranted")

        soundMeter.start()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
