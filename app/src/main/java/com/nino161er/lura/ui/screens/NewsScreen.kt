package com.nino161er.rssfeed.ui.screens

import android.annotation.SuppressLint
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nino161er.rssfeed.data.model.RssFeed
import com.nino161er.rssfeed.data.model.RssItem
import com.nino161er.rssfeed.ui.CustomNewsSource
import com.nino161er.rssfeed.ui.RssViewModel
import com.nino161er.rssfeed.ui.navigation.Tab

data class Newspaper(
    val name: String,
    val description: String,
    val url: String,
    val homeUrl: String,
    val category: String,
    val color: Color
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: RssViewModel,
    selectedSubTab: Tab,
    onNavigateToFeedItems: (Long, String) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val allFeeds by viewModel.allFeeds.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val customSources by viewModel.customNewsSources.collectAsState()
    var activeUrl by remember { mutableStateOf<String?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val curatedNewspapers = remember {
        // ... (same as before)
        listOf(
            Newspaper("The New York Times", "Global breaking news.", "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml", "https://www.nytimes.com", "Global", Color(0xFF121212)),
            Newspaper("BBC News", "Trusted world and UK news.", "https://feeds.bbci.co.uk/news/rss.xml", "https://www.bbc.com/news", "Global", Color(0xFFBB1919)),
            Newspaper("Der Spiegel", "German investigative journalism.", "https://www.spiegel.de/index.rss", "https://www.spiegel.de", "Germany", Color(0xFFE60005)),
            Newspaper("TechCrunch", "Technology and startup news.", "https://techcrunch.com/feed/", "https://techcrunch.com", "Tech", Color(0xFF02AD3F)),
            Newspaper("The Guardian", "Independent journalism.", "https://www.theguardian.com/uk/rss", "https://www.theguardian.com", "Global", Color(0xFF052962)),
            Newspaper("Wired", "Culture, science, and tech.", "https://www.wired.com/feed/rss", "https://www.wired.com", "Tech", Color(0xFF000000)),
            Newspaper("Ars Technica", "In-depth IT and science.", "https://feeds.arstechnica.com/arstechnica/index", "https://arstechnica.com", "Tech", Color(0xFFFF4400)),
            Newspaper("Reuters", "Breaking business and world news.", "https://www.reutersagency.com/feed/", "https://www.reuters.com", "Global", Color(0xFFFF8000)),
        )
    }

    // Notify ViewModel about browser state
    DisposableEffect(activeUrl) {
        viewModel.setNewsBrowserActive(activeUrl != null)
        onDispose { }
    }

    // Add source dialog
    if (showAddDialog) {
        AddSourceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, url ->
                viewModel.addCustomNewsSource(name, url)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            if (activeUrl == null) {
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
                    actions = {
                        IconButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showAddDialog = true 
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Source")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (activeUrl != null) {
                NewspaperBrowser(
                    url = activeUrl!!,
                    onWebViewCreated = { webViewRef = it },
                    onWebViewDestroyed = { webViewRef = null },
                    onClose = {
                        activeUrl = null
                        viewModel.setNewsBrowserActive(false)
                    },
                    onBack = { webView ->
                        if (webView.canGoBack()) {
                            webView.goBack()
                        } else {
                            activeUrl = null
                            viewModel.setNewsBrowserActive(false)
                        }
                    }
                )
            } else {
                Text(
                    "News",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)
                )
                
                when (selectedSubTab) {
                    Tab.NewsArticles -> {
                        NewsArticlesTab(
                            items = allItems.take(50),
                            feeds = allFeeds,
                            onOpenArticle = { url -> 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeUrl = url 
                            }
                        )
                    }
                    Tab.NewsSuggested -> {
                        NewsSuggestedTab(
                            newspapers = curatedNewspapers,
                            followedUrls = allFeeds.map { it.url }.toSet() + customSources.map { it.url }.toSet(),
                            onOpen = { url -> 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeUrl = url 
                            }
                        )
                    }
                    Tab.NewsFollowed -> {
                        NewsFollowedTab(
                            newspapers = curatedNewspapers,
                            allFeeds = allFeeds,
                            customSources = customSources,
                            onOpen = { url -> 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeUrl = url 
                            },
                            onRemoveCustom = { index -> 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.removeCustomNewsSource(index) 
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsArticlesTab(
    items: List<RssItem>,
    feeds: List<RssFeed>,
    onOpenArticle: (String) -> Unit
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No articles yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Add feeds and refresh to see articles here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 200.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "RECENT ARTICLES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(items) { item ->
            val feed = feeds.find { it.id == item.feedId }
            ArticleRow(item = item, feedName = feed?.title ?: "Unknown", onOpen = {
                item.link?.let { url -> onOpenArticle(url) }
            })
        }
    }
}

@Composable
private fun ArticleRow(item: RssItem, feedName: String, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        onClick = onOpen
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = feedName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            item.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            item.pubDate?.let { date ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun NewsSuggestedTab(
    newspapers: List<Newspaper>,
    followedUrls: Set<String>,
    onOpen: (String) -> Unit
) {
    val grouped = newspapers.groupBy { it.category }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 200.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        grouped.forEach { (category, papers) ->
            item {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(papers) { newspaper ->
                val isFollowed = followedUrls.contains(newspaper.url)
                NewspaperCard(
                    newspaper = newspaper,
                    isFollowed = isFollowed,
                    onOpen = { onOpen(newspaper.homeUrl) }
                )
            }
        }
    }
}

@Composable
private fun NewsFollowedTab(
    newspapers: List<Newspaper>,
    allFeeds: List<RssFeed>,
    customSources: List<CustomNewsSource>,
    onOpen: (String) -> Unit,
    onRemoveCustom: (Int) -> Unit
) {
    val followedCurated = newspapers.filter { paper -> allFeeds.any { it.url == paper.url } }
    val allFollowed = followedCurated + customSources.map { src ->
        Newspaper(src.name, "", "", src.url, "Custom", MaterialTheme.colorScheme.tertiary)
    }

    if (allFollowed.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No followed newspapers", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Browse Suggested or add custom sources", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 200.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (followedCurated.isNotEmpty()) {
            item {
                Text("CURATED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 4.dp))
            }
            items(followedCurated) { paper ->
                NewspaperCard(newspaper = paper, isFollowed = true, onOpen = { onOpen(paper.homeUrl) })
            }
        }
        if (customSources.isNotEmpty()) {
            item {
                Text("CUSTOM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            items(customSources.size) { index ->
                val source = customSources[index]
                CustomSourceCard(
                    source = source,
                    onOpen = { onOpen(source.url) },
                    onRemove = { onRemoveCustom(index) }
                )
            }
        }
    }
}

@Composable
private fun CustomSourceCard(source: CustomNewsSource, onOpen: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = source.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = source.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Custom Source", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewspaperBrowser(
    url: String,
    onWebViewCreated: (WebView) -> Unit,
    onWebViewDestroyed: () -> Unit,
    onClose: () -> Unit,
    onBack: (WebView) -> Unit
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf(url) }
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val surfaceColor = MaterialTheme.colorScheme.surface

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Box {
                // Progressive blur gradient behind the TopAppBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to surfaceColor,
                                0.4f to surfaceColor.copy(alpha = 0.95f),
                                0.7f to surfaceColor.copy(alpha = 0.6f),
                                1.0f to Color.Transparent
                            )
                        )
                        .blur(20.dp)
                )

                TopAppBar(
                    windowInsets = WindowInsets(0.dp),
                    title = {
                        Text(
                            text = currentUrl
                                .removePrefix("https://")
                                .removePrefix("http://")
                                .removePrefix("www.")
                                .substringBefore("/")
                                .take(40),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        Surface(
                            modifier = Modifier.padding(start = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            IconButton(onClick = { webView?.let { onBack(it) } }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Forward
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (canGoForward) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                            ) {
                                IconButton(
                                    onClick = { webView?.goForward() },
                                    enabled = canGoForward
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Forward",
                                        tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                            }
                            // Refresh
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                IconButton(onClick = { webView?.reload() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                                }
                            }
                            // Share
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                IconButton(onClick = {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, currentUrl)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                            }
                            // Close
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                IconButton(onClick = onClose) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // WebView fills the full screen behind everything
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                return false
                            }
                            override fun onPageFinished(view: WebView, finishedUrl: String) {
                                super.onPageFinished(view, finishedUrl)
                                currentUrl = finishedUrl
                                canGoBack = view.canGoBack()
                                canGoForward = view.canGoForward()
                                isLoading = false
                            }
                            override fun onPageStarted(view: WebView, startedUrl: String, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, startedUrl, favicon)
                                isLoading = true
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView, newProgress: Int) {
                                progress = newProgress.toFloat()
                                if (newProgress >= 100) isLoading = false
                            }
                        }

                        loadUrl(url)
                        onWebViewCreated(this)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    webView = view
                }
            )

            // Progress bar overlay at top
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = padding.calculateTopPadding())
            ) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.let {
                it.stopLoading()
                it.destroy()
            }
            onWebViewDestroyed()
        }
    }
}

@Composable
private fun NewspaperCard(
    newspaper: Newspaper,
    isFollowed: Boolean = false,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(newspaper.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = newspaper.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (newspaper.description.isNotEmpty()) {
                    Text(text = newspaper.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(text = newspaper.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            if (isFollowed) {
                Icon(Icons.Default.Check, contentDescription = "Followed", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun AddSourceDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Source", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Website URL") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name.trim(), url.trim()) },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
