package com.nino161er.rssfeed.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.nino161er.rssfeed.R

sealed class Tab(
    val route: String,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Feeds : Tab(
        route = "feeds",
        labelResId = R.string.nav_title_feeds,
        selectedIcon = Icons.Filled.RssFeed,
        unselectedIcon = Icons.Outlined.RssFeed
    )

    data object Saved : Tab(
        route = "saved",
        labelResId = R.string.nav_title_saved,
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    )

    data object Items : Tab(
        route = "items",
        labelResId = R.string.nav_title_articles,
        selectedIcon = Icons.AutoMirrored.Filled.Article,
        unselectedIcon = Icons.AutoMirrored.Outlined.Article
    )

    data object Radio : Tab(
        route = "radio",
        labelResId = R.string.nav_title_radio,
        selectedIcon = Icons.Default.Radio,
        unselectedIcon = Icons.Outlined.Radio
    )

    data object News : Tab(
        route = "news",
        labelResId = R.string.nav_title_news,
        selectedIcon = Icons.Default.Newspaper,
        unselectedIcon = Icons.Outlined.Newspaper
    )

    // Radio Sub-Tabs
    data object RadioSettings : Tab(
        route = "radio_settings",
        labelResId = R.string.nav_radio_settings,
        selectedIcon = Icons.Default.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
    data object RadioSaved : Tab(
        route = "radio_saved",
        labelResId = R.string.nav_radio_saved,
        selectedIcon = Icons.Default.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    )
    data object RadioTuner : Tab(
        route = "radio_tuner",
        labelResId = R.string.nav_radio_tuner,
        selectedIcon = Icons.Default.Tune,
        unselectedIcon = Icons.Outlined.Tune
    )
    data object RadioMap : Tab(
        route = "radio_map",
        labelResId = R.string.nav_radio_map,
        selectedIcon = Icons.Default.Map,
        unselectedIcon = Icons.Outlined.Map
    )

    // News Sub-Tabs
    data object NewsSettings : Tab(
        route = "news_settings",
        labelResId = R.string.nav_news_settings,
        selectedIcon = Icons.Default.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
    data object NewsFollowed : Tab(
        route = "news_followed",
        labelResId = R.string.nav_news_followed,
        selectedIcon = Icons.Default.LibraryBooks,
        unselectedIcon = Icons.Outlined.LibraryBooks
    )
    data object NewsSuggested : Tab(
        route = "news_suggested",
        labelResId = R.string.nav_news_suggested,
        selectedIcon = Icons.Default.Explore,
        unselectedIcon = Icons.Outlined.Explore
    )
    data object NewsArticles : Tab(
        route = "news_articles",
        labelResId = R.string.nav_news_articles,
        selectedIcon = Icons.Default.Article,
        unselectedIcon = Icons.Outlined.Article
    )

    companion object {
        val bottomNavItems get() = listOf(Feeds, Saved, Items)
        val drawerItems get() = listOf(Feeds, Radio, News)
        
        val radioTabs get() = listOf(RadioTuner, RadioSaved)
        val newsTabs get() = listOf(NewsFollowed, NewsSuggested, NewsArticles)
    }
}

sealed class Screen {
    data object FeedsList : Screen()
    data object ItemsList : Screen()
    data class FeedItems(val feedId: Long, val title: String) : Screen()
    data class ItemDetail(
        val itemId: Long,
        val title: String,
        val description: String,
        val content: String?,
        val imageUrl: String?
    ) : Screen()
    data object UnreadItems : Screen()
    data object StarredItems : Screen()
    data object ManageFeeds : Screen()
    data object Settings : Screen()
    data object Onboarding : Screen()
    data object Radio : Screen()
    data object News : Screen()
}
