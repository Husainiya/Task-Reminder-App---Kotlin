package com.example.todoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "TASK_REMINDER_CHANNEL"

    fun createNotification(reminderId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android 8.0 and above, we need a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Task Reminders", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Channel for task reminder notifications"
            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_alarm)
            .setAutoCancel(true) // Dismiss notification after it's clicked
            .build()

        // Display the notification with a unique ID
        notificationManager.notify(reminderId, notification)
    }
}
