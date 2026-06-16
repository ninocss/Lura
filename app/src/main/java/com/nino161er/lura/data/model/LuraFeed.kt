package com.nino161er.rssfeed.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rss_feeds")
data class RssFeed(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val category: String? = null,
    val iconUrl: String? = null,
    val accentColor: Int? = null
)
