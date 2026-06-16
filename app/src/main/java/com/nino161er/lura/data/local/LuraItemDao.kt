package com.nino161er.rssfeed.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nino161er.rssfeed.data.model.RssItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RssItemDao {

    @Query("SELECT * FROM rss_items WHERE feedId = :feedId AND isArchived = 0 ORDER BY pubDate DESC")
    fun getItemsForFeed(feedId: Long): Flow<List<RssItem>>

    @Query("SELECT * FROM rss_items WHERE isArchived = 0 ORDER BY pubDate DESC")
    fun getAllItems(): Flow<List<RssItem>>

    @Query("SELECT * FROM rss_items WHERE isArchived = 1 ORDER BY pubDate DESC")
    fun getArchivedItems(): Flow<List<RssItem>>

    @Query("SELECT * FROM rss_items WHERE isStarred = 1 AND isArchived = 0 ORDER BY pubDate DESC")
    fun getStarredItems(): Flow<List<RssItem>>

    @Query("SELECT * FROM rss_items WHERE id = :id")
    suspend fun getItemById(id: Long): RssItem?

    @Query("SELECT * FROM rss_items WHERE link = :link AND feedId = :feedId LIMIT 1")
    suspend fun getItemByLink(link: String, feedId: Long): RssItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: RssItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<RssItem>)

    @Update
    suspend fun updateItem(item: RssItem)

    @Query("UPDATE rss_items SET isRead = :isRead WHERE id = :id")
    suspend fun setReadStatus(id: Long, isRead: Boolean)

    @Query("UPDATE rss_items SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchivedStatus(id: Long, isArchived: Boolean)

    @Query("UPDATE rss_items SET isStarred = :isStarred WHERE id = :id")
    suspend fun setStarredStatus(id: Long, isStarred: Boolean)

    @Query("UPDATE rss_items SET aiSummary = :summary WHERE id = :id")
    suspend fun setAiSummary(id: Long, summary: String?)

    @Query("UPDATE rss_items SET isRead = 1 WHERE feedId = :feedId")
    suspend fun markAllAsRead(feedId: Long)

    @Query("UPDATE rss_items SET isRead = 0 WHERE feedId = :feedId")
    suspend fun markAllAsUnread(feedId: Long)

    @Delete
    suspend fun deleteItem(item: RssItem)

    @Query("DELETE FROM rss_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM rss_items WHERE feedId = :feedId")
    suspend fun deleteItemsForFeed(feedId: Long)
}
