package com.nino161er.rssfeed.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.nino161er.rssfeed.R
import com.nino161er.rssfeed.ui.RssViewModel
import com.nino161er.rssfeed.ui.components.StandardRow

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    viewModel: RssViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToDetail: (Long, String, String, String?, String?) -> Unit = { _, _, _, _, _ -> }
) {
    val items by viewModel.allItems.collectAsState()
    val feeds by viewModel.allFeeds.collectAsState()
    
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val hideRead by viewModel.hideReadArticles.collectAsState()

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val haptic = LocalHapticFeedback.current

    val filteredItems = items
        .filter { item ->
            searchQuery.isEmpty() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description?.contains(searchQuery, ignoreCase = true) == true
        }
        .filter { item -> !hideRead || !item.isRead }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    scrollBehavior = scrollBehavior
                )

                Text(
                    text = stringResource(R.string.nav_title_articles),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)
                )

                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { },
                            expanded = false,
                            onExpandedChange = { },
                            placeholder = { 
                                Text(
                                    stringResource(R.string.search_placeholder),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                ) 
                            },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Search, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        searchQuery = "" 
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                } else {
                                    Surface(
                                        modifier = Modifier.padding(end = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = stringResource(R.string.badge_unread), 
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    // Empty overlay content
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.refreshFeeds()
                isRefreshing = false
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 200.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val feed = feeds.find { it.id == item.feedId }
                    StandardRow(
                        title = item.title,
                        description = item.description ?: "",
                        imageUrl = item.imageUrl,
                        pubDate = "10 Jun",
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

                if (filteredItems.isEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(32.dp)) {
                            Text(
                                text = stringResource(R.string.items_empty),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
