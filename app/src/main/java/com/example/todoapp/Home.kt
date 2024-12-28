package com.example.todoapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class Home : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var reminderListView: ListView
    private lateinit var preferences: SharedPreferences
    private val reminders = mutableListOf<String>() // List to hold reminders

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Initialize SharedPreferences
        preferences = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)

        // Initialize VideoView
        videoView = findViewById(R.id.videoView)
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.animation)
        videoView.setVideoURI(videoUri)
        videoView.start()

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarAddTask)
        setSupportActionBar(toolbar)

        // Initialize ListView for reminders
        reminderListView = findViewById(R.id.homeReminderListView)

        // Load reminders from SharedPreferences
        loadReminders()

        // Set up buttons and their click listeners
        val dailyTaskButton: Button = findViewById(R.id.btnDailyTask)
        val alarmButton: Button = findViewById(R.id.btnAlarm)
        val stopwatchButton: Button = findViewById(R.id.btnStopwatch)

        dailyTaskButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        alarmButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }

        stopwatchButton.setOnClickListener {
            val intent = Intent(this, StopwatchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadReminders() {
        val loadedReminders = preferences.getStringSet("RemindersList", emptySet())?.toMutableList() ?: mutableListOf()
        reminders.clear()
        reminders.addAll(loadedReminders)
        updateReminderListView()
    }

    private fun updateReminderListView() {
        val adapter = ReminderAdapter(this, reminders)
        reminderListView.adapter = adapter
    }

    // Custom adapter for reminders
    inner class ReminderAdapter(private val context: Context, private val reminders: MutableList<String>) : BaseAdapter() {

        override fun getCount(): Int = reminders.size

        override fun getItem(position: Int): Any = reminders[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = layoutInflater.inflate(R.layout.reminder_list_item, parent, false)

            val reminderTextView: TextView = view.findViewById(R.id.reminderTextView)
            reminderTextView.text = reminders[position]

            return view
        }
    }
}
