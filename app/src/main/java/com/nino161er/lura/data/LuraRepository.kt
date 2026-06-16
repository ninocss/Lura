package com.nino161er.rssfeed.data

import com.nino161er.rssfeed.data.local.RssFeedDao
import com.nino161er.rssfeed.data.local.RssItemDao
import com.nino161er.rssfeed.data.model.RssFeed
import com.nino161er.rssfeed.data.model.RssItem
import com.nino161er.rssfeed.data.parser.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.net.URL

class RssRepository(
    private val feedDao: RssFeedDao,
    private val itemDao: RssItemDao,
    private val parser: RssParser = RssParser()
) {
    val allFeeds: Flow<List<RssFeed>> = feedDao.getAllFeeds()
    val allItems: Flow<List<RssItem>> = itemDao.getAllItems()
    val archivedItems: Flow<List<RssItem>> = itemDao.getArchivedItems()
    val starredItems: Flow<List<RssItem>> = itemDao.getStarredItems()

    suspend fun refreshFeeds(): List<RssItem> = withContext(Dispatchers.IO) {
        val feeds = feedDao.getFeedsSync()
        val allNewItems = mutableListOf<RssItem>()
        for (feed in feeds) {
            allNewItems.addAll(refreshFeed(feed))
        }
        allNewItems
    }

    suspend fun refreshFeed(feed: RssFeed): List<RssItem> = withContext(Dispatchers.IO) {
        try {
            val xml = URL(feed.url).readText()
            val parsedFeed = parser.parse(xml, feed.id)
            
            if (feed.iconUrl != parsedFeed.iconUrl) {
                feedDao.updateFeed(feed.copy(iconUrl = parsedFeed.iconUrl))
            }
            
            val newItems = mutableListOf<RssItem>()
            val itemsToInsert = parsedFeed.items.map { newItem ->
                val existing = itemDao.getItemByLink(newItem.link ?: "", feed.id)
                if (existing != null) {
                    newItem.copy(
                        id = existing.id,
                        isRead = existing.isRead,
                        isArchived = existing.isArchived,
                        isStarred = existing.isStarred
                    )
                } else {
                    newItems.add(newItem)
                    newItem
                }
            }
            itemDao.insertItems(itemsToInsert)
            newItems
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addFeed(url: String, category: String? = null) = withContext(Dispatchers.IO) {
        try {
            val xml = URL(url).readText()
            val parsedFeed = parser.parse(xml, 0)
            val feedId = feedDao.insertFeed(
                RssFeed(
                    title = parsedFeed.title,
                    url = url,
                    category = category,
                    iconUrl = parsedFeed.iconUrl
                )
            )
            val itemsWithId = parsedFeed.items.map { it.copy(feedId = feedId) }
            itemDao.insertItems(itemsWithId)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateFeedCategory(feed: RssFeed, category: String?) = withContext(Dispatchers.IO) {
        feedDao.updateFeed(feed.copy(category = category))
    }

    suspend fun updateItemReadStatus(itemId: Long, isRead: Boolean) = withContext(Dispatchers.IO) {
        itemDao.setReadStatus(itemId, isRead)
    }

    suspend fun updateItemArchivedStatus(itemId: Long, isArchived: Boolean) = withContext(Dispatchers.IO) {
        itemDao.setArchivedStatus(itemId, isArchived)
    }

    suspend fun updateItemStarredStatus(itemId: Long, isStarred: Boolean) = withContext(Dispatchers.IO) {
        itemDao.setStarredStatus(itemId, isStarred)
    }

    suspend fun updateItemAiSummary(itemId: Long, summary: String?) = withContext(Dispatchers.IO) {
        itemDao.setAiSummary(itemId, summary)
    }

    suspend fun deleteItem(item: RssItem) = withContext(Dispatchers.IO) {
        itemDao.deleteItem(item)
    }

    suspend fun deleteFeed(feed: RssFeed) = withContext(Dispatchers.IO) {
        feedDao.deleteFeed(feed)
    }

    suspend fun markAllFeedItemsAsRead(feedId: Long) = withContext(Dispatchers.IO) {
        itemDao.markAllAsRead(feedId)
    }

    suspend fun markAllFeedItemsAsUnread(feedId: Long) = withContext(Dispatchers.IO) {
        itemDao.markAllAsUnread(feedId)
    }

    fun getItemsForFeed(feedId: Long): Flow<List<RssItem>> = itemDao.getItemsForFeed(feedId)
}
