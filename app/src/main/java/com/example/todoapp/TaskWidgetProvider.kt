package com.example.todoapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, AlarmActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val views = RemoteViews(context.packageName, R.layout.widget_task)
            views.setOnClickPendingIntent(R.id.widgetText, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
