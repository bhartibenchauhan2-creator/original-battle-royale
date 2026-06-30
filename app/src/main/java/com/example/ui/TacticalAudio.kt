package com.example.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TacticalAudio {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.e("TacticalAudio", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playClick() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 40)
            } catch (e: Exception) {
                // Ignore tone errors gracefully
            }
        }
    }

    fun playGunshot() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fast double pulse to mimic weapon burst
                toneGen?.startTone(ToneGenerator.TONE_SUP_DIAL, 50)
                delay(70)
                toneGen?.startTone(ToneGenerator.TONE_SUP_DIAL, 40)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playSniperShot() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Longer, deep pulse
                toneGen?.startTone(ToneGenerator.TONE_SUP_BUSY, 150)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playReload() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 60)
                delay(120)
                toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 50)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playAlert() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 300)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playExplosion() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 400)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
