package com.nino161er.rssfeed.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nino161er.rssfeed.data.model.RssFeed
import kotlinx.coroutines.flow.Flow

@Dao
interface RssFeedDao {

    @Query("SELECT * FROM rss_feeds ORDER BY title ASC")
    fun getAllFeeds(): Flow<List<RssFeed>>

    @Query("SELECT * FROM rss_feeds")
    suspend fun getFeedsSync(): List<RssFeed>

    @Query("SELECT * FROM rss_feeds WHERE id = :id")
    suspend fun getFeedById(id: Long): RssFeed?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: RssFeed): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeds(feeds: List<RssFeed>)

    @Update
    suspend fun updateFeed(feed: RssFeed)

    @Delete
    suspend fun deleteFeed(feed: RssFeed)

    @Query("DELETE FROM rss_feeds WHERE id = :id")
    suspend fun deleteFeedById(id: Long)
}
