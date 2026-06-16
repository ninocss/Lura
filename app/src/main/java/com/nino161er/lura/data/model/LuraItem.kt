package com.nino161er.rssfeed.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rss_items",
    foreignKeys = [
        ForeignKey(
            entity = RssFeed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("feedId")]
)
data class RssItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val feedId: Long,
    val title: String,
    val link: String? = null,
    val description: String? = null,
    val pubDate: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val isArchived: Boolean = false,
    val isStarred: Boolean = false,
    val aiSummary: String? = null
)
