package com.example.todoapp

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.Vibrator

class AlarmService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmTone = intent?.getIntExtra("AlarmTone", R.raw.ringtone1) ?: R.raw.ringtone2

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1500)  // 1.5-second vibration

        // Play selected alarm sound
        mediaPlayer = MediaPlayer.create(this, alarmTone)
        mediaPlayer.setOnCompletionListener {
            stopSelf() // Stop the service once the sound has finished playing
        }
        mediaPlayer.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
