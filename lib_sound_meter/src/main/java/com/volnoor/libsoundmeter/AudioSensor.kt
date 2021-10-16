package com.volnoor.libsoundmeter

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.log10

private const val SAMPLE_RATE_IN_HZ = 8000
private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
    SAMPLE_RATE_IN_HZ,
    AudioFormat.CHANNEL_IN_MONO,
    AudioFormat.ENCODING_PCM_16BIT
)

internal class AudioSensor {

    companion object {
        const val STATE_STARTED = 0
        const val STATE_STOPPED = 1
    }

    private val buffer = ShortArray(BUFFER_SIZE)
    private var audioRecord: AudioRecord? = null
    private var state = STATE_STOPPED

    val amplitudeDb: Double
        get() {
            val size = requireNotNull(audioRecord).read(buffer, 0, BUFFER_SIZE)
            var sum = 0L
            for (value in buffer) {
                sum += (value * value).toLong()
            }
            var mean = sum / size.toDouble()
            if (mean == 0.0) {
                mean = 1.0 // Avoid -Infinity from Math.log10 method
            }
            return 10 * log10(mean)
        }

    fun start() {
        if (state != STATE_STARTED) {
            state = STATE_STARTED
            initIfNeed()
            requireNotNull(audioRecord).startRecording()
        }
    }

    fun stop() {
        if (state != STATE_STOPPED) {
            state = STATE_STOPPED
            audioRecord?.release()
            audioRecord = null
        }
    }

    @SuppressLint("MissingPermission")
    private fun initIfNeed() {
        if (audioRecord == null) {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE
            )
        }
    }
}
