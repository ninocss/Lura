package com.nino161er.rssfeed.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nino161er.lura.MainActivity
import com.nino161er.rssfeed.R
import com.nino161er.rssfeed.data.worker.RefreshWorker
import java.util.concurrent.TimeUnit

object NotificationHelper {

    private const val CHANNEL_ID = "new_articles"
    private const val NOTIFICATION_ID = 1001
    private const val WORK_NAME = "feed_refresh"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showNewArticlesNotification(context: Context, newItems: List<com.nino161er.rssfeed.data.model.RssItem>) {
        if (newItems.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val newCount = newItems.size
        val title = context.getString(R.string.notification_new_articles_title)
        val contentText = context.resources.getQuantityString(
            R.plurals.notification_new_articles_body, newCount, newCount
        )

        val bigText = StringBuilder()
        newItems.take(5).forEach { item ->
            bigText.append("• ").append(item.title).append("\n")
        }
        if (newCount > 5) {
            bigText.append("...")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText.toString()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted on API 33+
        }
    }

    fun scheduleRefresh(context: Context, intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME)

        if (intervalMinutes <= 0) return

        val effectiveInterval = maxOf(intervalMinutes, 15).toLong()
        val request = PeriodicWorkRequestBuilder<RefreshWorker>(
            effectiveInterval, TimeUnit.MINUTES
        ).build()
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
