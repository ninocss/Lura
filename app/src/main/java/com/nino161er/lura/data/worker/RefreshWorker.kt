package com.nino161er.rssfeed.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nino161er.rssfeed.data.RssRepository
import com.nino161er.rssfeed.data.local.AppDatabase
import com.nino161er.rssfeed.data.notification.NotificationHelper

class RefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val repository = RssRepository(database.rssFeedDao(), database.rssItemDao())

            val newItems = repository.refreshFeeds()

            if (newItems.isNotEmpty()) {
                NotificationHelper.showNewArticlesNotification(applicationContext, newItems)
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
