package com.example.todoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmTone = intent.getIntExtra("AlarmTone", R.raw.ringtone1) // Retrieve selected tone
        val alarmServiceIntent = Intent(context, AlarmService::class.java)
        alarmServiceIntent.putExtra("AlarmTone", alarmTone) // Pass selected tone to the service
        context.startService(alarmServiceIntent)

        // Create notification
        showNotification(context, "Reminder", "It's time for your task!")
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationId = 1
        val channelId = "alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarm Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an explicit intent for the Home activity
        val homeIntent = Intent(context, Home::class.java)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear previous activities
        val pendingIntent = PendingIntent.getActivity(context, 0, homeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Create the notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_alarm) // Use your own icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the intent to launch Home activity

        // Show the notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
