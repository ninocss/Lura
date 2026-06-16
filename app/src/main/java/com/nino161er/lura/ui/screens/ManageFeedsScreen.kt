package com.nino161er.rssfeed.ui.screens

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.nino161er.rssfeed.R
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.widget.Toast
import com.nino161er.rssfeed.data.model.RssFeed
import com.nino161er.rssfeed.ui.RssViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFeedsScreen(
    viewModel: RssViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val feeds by viewModel.allFeeds.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var urlInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<RssFeed?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val uncategorizedLabel = stringResource(R.string.feed_uncategorized)
    val groupedFeeds = remember(feeds) {
        feeds.groupBy { it.category ?: uncategorizedLabel }
    }
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, context.getString(R.string.error_invalid_feed), Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString() ?: ""
            if (text.startsWith("http://") || text.startsWith("https://")) {
                urlInput = text
            }
        }
    }

    if (showDeleteDialog != null) {
        val feed = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text(stringResource(R.string.dialog_remove_feed_title)) },
            text = { Text(stringResource(R.string.dialog_remove_feed_message, feed.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFeed(feed)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.manage_feeds_title),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ) 
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(36.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.add_feed_title), 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text(stringResource(R.string.feed_url_label)) },
                            placeholder = { Text(stringResource(R.string.feed_url_placeholder)) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    urlInput = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                }) {
                                    Icon(Icons.Default.ContentPaste, stringResource(R.string.action_paste))
                                }
                            }
                        )

                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text(stringResource(R.string.category_label)) },
                            placeholder = { Text(stringResource(R.string.category_placeholder)) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (urlInput.isNotBlank()) {
                                    viewModel.addFeed(urlInput.trim(), categoryInput.trim().ifEmpty { null })
                                    urlInput = ""
                                    categoryInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            enabled = urlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = stringResource(R.string.action_follow_feed),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (feeds.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.feeds_empty),
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            groupedFeeds.forEach { (category, categoryFeeds) ->
                item(key = category) {
                    val isExpanded = expandedStates[category] ?: true
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { expandedStates[category] = !isExpanded },
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = categoryFeeds.size.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null
                            )
                        }
                    }
                }

                items(categoryFeeds, key = { it.id }) { feed ->
                    AnimatedVisibility(visible = expandedStates[category] ?: true) {
                        ListItem(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            headlineContent = { Text(feed.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(feed.url, maxLines = 1) },
                            leadingContent = {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    if (feed.iconUrl != null) {
                                        AsyncImage(
                                            model = feed.iconUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(feed.title.take(1).uppercase(), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { showDeleteDialog = feed }) {
                                    Icon(Icons.Default.DeleteOutline, stringResource(R.string.content_desc_delete), tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        )
                    }
                }
            }
        }
    }
}
