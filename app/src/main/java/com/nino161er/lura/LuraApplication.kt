package com.nino161er.rssfeed

import android.app.Application
import com.nino161er.rssfeed.data.notification.NotificationHelper

class RssApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
