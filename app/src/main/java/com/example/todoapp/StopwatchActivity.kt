package com.example.todoapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar

class StopwatchActivity : AppCompatActivity() {

    private var timeInSeconds = 0L
    private var running = false
    private lateinit var handler: Handler
    private var startTime = 0L

    // Declare the views
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_watch_task) // Ensure this matches your XML filename

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarAddTask)
        setSupportActionBar(toolbar)

        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize the views
        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnReset = findViewById(R.id.btnReset)

        handler = Handler(Looper.getMainLooper())

        btnStart.setOnClickListener {
            if (!running) {
                running = true
                startTime = System.currentTimeMillis() - timeInSeconds
                handler.post(runnable)
            }
        }

        btnStop.setOnClickListener {
            running = false
            handler.removeCallbacks(runnable)
        }

        btnReset.setOnClickListener {
            running = false
            handler.removeCallbacks(runnable)
            timeInSeconds = 0L
            tvTimer.text = "00:00:00"
        }
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (running) {
                timeInSeconds = System.currentTimeMillis() - startTime
                tvTimer.text = getFormattedTime(timeInSeconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun getFormattedTime(timeInMillis: Long): String {
        val totalSeconds = timeInMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Handle the back button click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate back to Home activity
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                finish() // Optionally call finish() to remove this activity from the back stack
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
