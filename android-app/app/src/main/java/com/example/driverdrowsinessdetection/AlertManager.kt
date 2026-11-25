package com.example.driverdrowsinessdetection

import android.media.AudioManager
import android.media.ToneGenerator

class AlertManager {

    private val toneGenerator = ToneGenerator(
        AudioManager.STREAM_ALARM,
        100
    )

    fun playDrowsinessAlert() {
        toneGenerator.startTone(
            ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
            1000
        )
    }

    fun playYawningAlert() {
        toneGenerator.startTone(
            ToneGenerator.TONE_PROP_BEEP2,
            500
        )
    }

    fun release() {
        toneGenerator.release()
    }
}