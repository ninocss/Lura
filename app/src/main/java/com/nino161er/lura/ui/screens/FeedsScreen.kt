package com.nino161er.rssfeed.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nino161er.rssfeed.R
import com.nino161er.rssfeed.data.model.RssFeed
import com.nino161er.rssfeed.data.model.RssItem
import com.nino161er.rssfeed.ui.RssViewModel

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.nino161er.rssfeed.ui.components.StandardRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsScreen(
    viewModel: RssViewModel,
    onNavigateToFeedItems: (Long, String) -> Unit,
    onNavigateToUnread: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val feeds by viewModel.allFeeds.collectAsState()
    val items by viewModel.allItems.collectAsState()
    val unreadCount = items.count { !it.isRead }
    val defaultCategory = stringResource(R.string.feed_category_default)
    val haptic = LocalHapticFeedback.current

    val groupedFeeds = remember(feeds, defaultCategory) {
        feeds.groupBy { it.category ?: defaultCategory }
    }
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenDrawer()
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 200.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    "RSS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Unread Card
            item {
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToUnread()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(36.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.feed_unread),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.feed_unread_items, unreadCount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Grouped Feeds
            groupedFeeds.forEach { (category, categoryFeeds) ->
                item(key = category) {
                    val isExpanded = expandedStates[category] ?: true
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        expandedStates[category] = !isExpanded 
                                    }
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    categoryFeeds.forEach { feed ->
                                        val feedUnreadCount = items.count { it.feedId == feed.id && !it.isRead }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { 
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onNavigateToFeedItems(feed.id, feed.title) 
                                                }
                                                .padding(horizontal = 20.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(32.dp),
                                                shape = CircleShape,
                                                color = Color.Transparent
                                            ) {
                                                if (feed.iconUrl != null) {
                                                    AsyncImage(
                                                        model = feed.iconUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(feed.title.take(1))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = feed.title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1
                                            )
                                            if (feedUnreadCount > 0) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = feedUnreadCount.toString(),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
