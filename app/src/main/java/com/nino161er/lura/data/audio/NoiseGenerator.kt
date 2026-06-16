package com.nino161er.rssfeed.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.random.Random

/**
 * Generates white noise audio playback using Android's AudioTrack API.
 * Used to simulate radio static between stations.
 */
class NoiseGenerator {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var noiseThread: Thread? = null
    private var volume = 0.5f

    fun start() {
        if (isPlaying) return
        isPlaying = true

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        audioTrack = AudioTrack(attrs, format, bufferSize, AudioTrack.MODE_STREAM, 0).apply {
            setVolume(volume)
            play()
        }

        noiseThread = Thread {
            val buffer = ShortArray(bufferSize / 2)
            while (isPlaying) {
                for (i in buffer.indices) {
                    buffer[i] = (Random.nextInt(65536) - 32768).toShort()
                }
                try {
                    audioTrack?.write(buffer, 0, buffer.size)
                } catch (_: Exception) {
                    break
                }
            }
        }.apply { start() }
    }

    fun stop() {
        isPlaying = false
        noiseThread?.interrupt()
        try { noiseThread?.join(500) } catch (_: InterruptedException) {}
        noiseThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        audioTrack?.setVolume(volume)
    }

    fun release() {
        stop()
    }
}
