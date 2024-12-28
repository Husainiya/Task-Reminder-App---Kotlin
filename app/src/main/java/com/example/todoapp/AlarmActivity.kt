package com.example.todoapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*

class AlarmActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var setAlarmButton: Button
    private lateinit var alarmManager: AlarmManager
    private lateinit var reminderListView: ListView
    private val reminders = mutableListOf<String>() // List to hold reminders
    private var selectedTone: Int = R.raw.ringtone1 // Default tone
    private lateinit var preferences: SharedPreferences
    private lateinit var notificationHelper: NotificationHelper // Declare NotificationHelper

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        preferences = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        selectedTone = preferences.getInt("SelectedTone", R.raw.ringtone1) // Load previously selected tone

        timePicker = findViewById(R.id.timePicker)
        setAlarmButton = findViewById(R.id.setAlarmButton)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        reminderListView = findViewById(R.id.reminderListView)

        notificationHelper = NotificationHelper(this) // Initialize NotificationHelper

        // Set default time
        timePicker.hour = 12
        timePicker.minute = 30

        // Set up the toolbar for back navigation
        val toolbar: Toolbar = findViewById(R.id.toolbarAddTask)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()  // Close AlarmActivity to go back to Home
        }

        setAlarmButton.setOnClickListener {
            showToneSelectionDialog() // Show tone selection dialog
        }

        // Load reminders from SharedPreferences
        loadRemindersFromPreferences()

        // Set up the ListView with custom adapter
        updateReminderListView()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showToneSelectionDialog() {
        val tones = resources.getStringArray(R.array.alarm_tones)
        val toneIds = arrayOf(R.raw.ringtone1, R.raw.ringtone2, R.raw.ringtone3)

        AlertDialog.Builder(this)
            .setTitle("Select Alarm Tone")
            .setItems(tones) { _, which ->
                selectedTone = toneIds[which]
                preferences.edit().putInt("SelectedTone", selectedTone).apply()
                Toast.makeText(this, "Selected: ${tones[which]}", Toast.LENGTH_SHORT).show()
                setAlarmForReminder() // Set alarm after tone selection
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarmForReminder() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
        calendar.set(Calendar.MINUTE, timePicker.minute)
        calendar.set(Calendar.SECOND, 0) // Set seconds to zero for exact timing

        val timeInMillis = calendar.timeInMillis

        // Ensure the time is not in the past
        if (timeInMillis < System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("AlarmTone", selectedTone) // Pass the selected tone
        val pendingIntent = PendingIntent.getBroadcast(this, reminders.size, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        // Add reminder to the list and update ListView
        val reminderText = "Alarm set for ${String.format("%02d", timePicker.hour)}:${String.format("%02d", timePicker.minute)}"
        reminders.add(reminderText)

        // Save reminder to SharedPreferences
        saveRemindersToPreferences()

        updateReminderListView()

        // Generate a notification when alarm is set
        notificationHelper.createNotification(
            reminderId = reminders.size,  // Unique ID based on reminder count
            title = "Alarm Reminder",
            message = reminderText
        )
    }

    // Function to load reminders from SharedPreferences
    private fun loadRemindersFromPreferences() {
        val remindersSet = preferences.getStringSet("RemindersList", emptySet()) ?: emptySet()
        reminders.clear()
        reminders.addAll(remindersSet)
    }

    // Function to save reminders to SharedPreferences
    private fun saveRemindersToPreferences() {
        val editor = preferences.edit()
        editor.putStringSet("RemindersList", reminders.toSet())
        editor.apply()
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

        @RequiresApi(Build.VERSION_CODES.M)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = layoutInflater.inflate(R.layout.reminder_list_item, parent, false)

            val reminderTextView: TextView = view.findViewById(R.id.reminderTextView)
            val editButton: Button = view.findViewById(R.id.editReminderButton)
            val deleteButton: Button = view.findViewById(R.id.deleteReminderButton)

            reminderTextView.text = reminders[position]

            // Handle Edit button click
            editButton.setOnClickListener {
                editReminder(position)
            }

            // Handle Delete button click
            deleteButton.setOnClickListener {
                deleteReminder(position)
            }

            return view
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun editReminder(position: Int) {
        // Prompt for new time
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
        calendar.set(Calendar.MINUTE, timePicker.minute)

        reminders[position] = "Alarm updated to ${String.format("%02d", timePicker.hour)}:${String.format("%02d", timePicker.minute)}"
        updateReminderListView()

        Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show()

        // Save updated reminders to SharedPreferences
        saveRemindersToPreferences()
    }

    private fun deleteReminder(position: Int) {
        // Delete the reminder at the given position
        reminders.removeAt(position)
        updateReminderListView()

        Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show()

        // Save updated reminders to SharedPreferences
        saveRemindersToPreferences()
    }
}
