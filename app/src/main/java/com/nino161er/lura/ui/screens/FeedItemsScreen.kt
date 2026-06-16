package com.nino161er.rssfeed.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.nino161er.rssfeed.R
import com.nino161er.rssfeed.ui.RssViewModel
import com.nino161er.rssfeed.ui.components.StandardRow
import java.util.Calendar

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedItemsScreen(
    viewModel: RssViewModel,
    feedId: Long,
    feedTitle: String,
    onNavigateToDetail: (Long, String, String, String?, String?) -> Unit,
    onBack: () -> Unit
) {
    val items by viewModel.allItems.collectAsState()
    val feeds by viewModel.allFeeds.collectAsState()
    val feed = feeds.find { it.id == feedId }
    val hideRead by viewModel.hideReadArticles.collectAsState()
    val haptic = LocalHapticFeedback.current

    val feedItems = items
        .filter { it.feedId == feedId }
        .filter { item -> !hideRead || !item.isRead }
    val doneAllScope = rememberCoroutineScope()
    val doneAllScale = remember { Animatable(1f) }
    val groupToday = stringResource(R.string.group_today)

    // Simple grouping by date (mocking "Today", "Yesterday" etc. for now)
    val groupedItems = remember(feedItems, groupToday) {
        feedItems.groupBy { groupToday } // In a real app, use a proper date formatter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
                    ) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    }
                },
                actions = {
                    val allRead = feedItems.isNotEmpty() && feedItems.all { it.isRead }
                    val doneAllBg by animateColorAsState(
                        targetValue = if (allRead) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                        animationSpec = tween(400),
                        label = "done_all_bg"
                    )
                    val doneAllTint by animateColorAsState(
                        targetValue = if (allRead) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                        animationSpec = tween(300),
                        label = "done_all_tint"
                    )
                    Surface(
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = doneAllBg
                    ) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleMarkAllRead(feedId, allRead)
                            doneAllScope.launch {
                                doneAllScale.snapTo(1.5f)
                                doneAllScale.animateTo(
                                    1f,
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = stringResource(if (allRead) R.string.action_mark_all_unread else R.string.action_mark_all_read),
                                tint = doneAllTint,
                                modifier = Modifier.graphicsLayer(scaleX = doneAllScale.value, scaleY = doneAllScale.value)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Text(
                    feedTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)
                )
            }

            groupedItems.forEach { (date, itemsForDate) ->
                item {
                    Text(
                        text = date,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(itemsForDate) { item ->
                    StandardRow(
                        title = item.title,
                        description = item.description ?: "",
                        imageUrl = item.imageUrl,
                        pubDate = "14:28", // Extract time from pubDate
                        sourceName = feedTitle,
                        sourceIconUrl = feed?.iconUrl,
                        isRead = item.isRead,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleReadStatus(item)
                            onNavigateToDetail(
                                item.id,
                                item.title,
                                item.description ?: "",
                                item.content,
                                item.imageUrl
                            )
                        },
                        onSwipeMarkRead = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.markAsRead(item.id) 
                        }
                    )
                }
            }
        }
    }
}
