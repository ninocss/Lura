package com.nino161er.rssfeed.ui.screens

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.nino161er.rssfeed.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nino161er.rssfeed.ui.RssViewModel
import com.nino161er.rssfeed.ui.components.StandardRow
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatDate(raw: String?): String? {
    if (raw == null) return null
    return try {
        val formats = arrayOf(
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        )
        for (fmt in formats) {
            try {
                val date = fmt.parse(raw)
                if (date != null) {
                    return SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
                }
            } catch (_: Exception) {}
        }
        raw
    } catch (_: Exception) { raw }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnreadItemsScreen(
    viewModel: RssViewModel,
    onNavigateToDetail: (Long, String, String, String?, String?) -> Unit,
    onBack: () -> Unit
) {
    val items by viewModel.allItems.collectAsState()
    val feeds by viewModel.allFeeds.collectAsState()
    val unreadItems = items.filter { !it.isRead }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                stringResource(R.string.unread_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
            )

            if (unreadItems.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.unread_empty),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(unreadItems, key = { it.id }) { item ->
                        val feed = feeds.find { it.id == item.feedId }
                        StandardRow(
                            title = item.title,
                            description = item.description ?: "",
                            imageUrl = item.imageUrl,
                            pubDate = formatDate(item.pubDate),
                            sourceName = feed?.title,
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
}
