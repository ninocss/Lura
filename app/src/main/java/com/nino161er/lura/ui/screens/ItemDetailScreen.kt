package com.nino161er.rssfeed.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import com.nino161er.rssfeed.data.ai.AiProvider
import com.nino161er.rssfeed.data.ai.GeminiSummarizer
import com.nino161er.rssfeed.data.ai.LocalModelManager
import com.nino161er.rssfeed.data.ai.LocalSummarizer
import com.nino161er.rssfeed.data.util.LanguageDetector
import java.util.Locale
import com.nino161er.rssfeed.ui.RssViewModel
import com.nino161er.rssfeed.ui.components.DominantColorAsyncImage
import androidx.compose.ui.res.stringResource
import com.nino161er.rssfeed.R
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    title: String,
    description: String,
    content: String?,
    imageUrl: String?,
    viewModel: RssViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.allItems.collectAsState()
    val currentItem = remember(itemId, items) { items.find { it.id == itemId } }
            val isSaved = currentItem?.isStarred ?: false
    val isRead = currentItem?.isRead ?: false
    val scope = rememberCoroutineScope()
    
    val readerFontSize by viewModel.readerFontSize.collectAsState()
    val readerFontFamilyName by viewModel.readerFontFamily.collectAsState()
    val readerLineHeight by viewModel.readerLineHeight.collectAsState()
    val readerPadding by viewModel.readerHorizontalPadding.collectAsState()

    val checkScale = remember { Animatable(1f) }
    var useWebView by remember { mutableStateOf(viewModel.getArticleViewMode() == "webview") }
    var dominantColor by remember { mutableStateOf(Color.Unspecified) }
    val hasHtmlContent = content?.contains('<') == true
    val view = LocalView.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val bgColor by animateColorAsState(
        targetValue = if (dominantColor != Color.Unspecified) {
            dominantColor.copy(alpha = 0.04f)
        } else MaterialTheme.colorScheme.background,
        animationSpec = tween(600),
        label = "bg_tint"
    )

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsSpeaking by remember { mutableStateOf(false) }

    val speakText = remember(title, description, content) {
        buildString {
            append(title)
            append(". ")
            if (description.isNotBlank()) {
                append(description.replace(Regex("<[^>]*>"), ""))
                append(". ")
            }
            if (content != null && content != description && content.isNotBlank()) {
                append(content.replace(Regex("<[^>]*>"), ""))
            }
        }
    }
    val detectedLang = remember(speakText) { LanguageDetector.detect(speakText) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            // Initialized successfully
        }
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post { isTtsSpeaking = true }
            }
            override fun onDone(utteranceId: String?) {
                mainHandler.post { isTtsSpeaking = false }
            }
            override fun onError(utteranceId: String?) {
                mainHandler.post { isTtsSpeaking = false }
            }
        })
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val apiKey = remember { viewModel.getGeminiApiKey() }
    var isSummarizing by remember { mutableStateOf(false) }
    var summarizationError by remember { mutableStateOf<String?>(null) }
    val toastNoApiKey = stringResource(R.string.toast_no_api_key)
    val ttsInitializing = stringResource(R.string.tts_initializing)
    val errorSummaryFailed = stringResource(R.string.error_summary_failed)
    val appLanguageName = viewModel.getAppLanguageName()
    val summaryLanguage = remember(appLanguageName) { appLanguageName }

    val onSummarize: () -> Unit = {
        scope.launch {
            isSummarizing = true
            summarizationError = null
            try {
                val fullArticleText = buildString {
                    append("Title: ").append(title).append("\n\n")
                    if (description.isNotBlank()) {
                        append("Description: ").append(description.replace(Regex("<[^>]*>"), "")).append("\n\n")
                    }
                    if (content != null && content != description && content.isNotBlank()) {
                        append("Content: ").append(content.replace(Regex("<[^>]*>"), "")).append("\n\n")
                    }
                }

                val summary = when (viewModel.getAiProvider()) {
                    AiProvider.ON_DEVICE -> {
                        if (!LocalModelManager.isModelAvailable(context)) {
                            throw IllegalStateException("Local model not found. Please import it in Settings.")
                        }
                        LocalSummarizer.summarize(fullArticleText, context, summaryLanguage)
                    }
                    else -> {
                        if (apiKey.isBlank()) {
                            throw IllegalArgumentException(toastNoApiKey)
                        }
                        GeminiSummarizer.summarize(fullArticleText, apiKey, viewModel.getGeminiModel(), summaryLanguage)
                    }
                }
                viewModel.updateItemAiSummary(itemId, summary)
            } catch (e: Exception) {
                summarizationError = e.message ?: errorSummaryFailed
            } finally {
                isSummarizing = false
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(bgColor),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
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
                        // 1. Mark as Read
                        val readBg by animateColorAsState(
                            targetValue = if (isRead) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            animationSpec = tween(400),
                            label = "read_bg"
                        )
                        val readTint by animateColorAsState(
                            targetValue = if (isRead) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            animationSpec = tween(300),
                            label = "read_tint"
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = readBg
                        ) {
                            IconButton(onClick = {
                                if (currentItem != null) {
                                    viewModel.toggleReadStatus(currentItem)
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    scope.launch {
                                        checkScale.snapTo(1.5f)
                                        checkScale.animateTo(
                                            1f,
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                        )
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = stringResource(if (isRead) R.string.action_mark_unread else R.string.action_mark_read),
                                    tint = readTint,
                                    modifier = Modifier.graphicsLayer(scaleX = checkScale.value, scaleY = checkScale.value)
                                )
                            }
                        }

                        // 2. Bookmark / Save
                        val savedScale = remember { Animatable(1f) }
                        val savedBg by animateColorAsState(
                            targetValue = if (isSaved) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            animationSpec = tween(400),
                            label = "saved_bg"
                        )
                        val savedTint by animateColorAsState(
                            targetValue = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            animationSpec = tween(300),
                            label = "saved_tint"
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = savedBg
                        ) {
                            IconButton(onClick = {
                                viewModel.toggleStarredStatus(itemId, !isSaved)
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                scope.launch {
                                    savedScale.snapTo(1.5f)
                                    savedScale.animateTo(
                                        1f,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                    )
                                }
                            }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = stringResource(if (isSaved) R.string.action_unsave else R.string.action_save),
                                    tint = savedTint,
                                    modifier = Modifier.graphicsLayer(scaleX = savedScale.value, scaleY = savedScale.value)
                                )
                            }
                        }

                        // 3. TTS / Speaker
                        val speakerBg by animateColorAsState(
                            targetValue = if (isTtsSpeaking) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            animationSpec = tween(400),
                            label = "speaker_bg"
                        )
                        val speakerTint by animateColorAsState(
                            targetValue = if (isTtsSpeaking) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            animationSpec = tween(300),
                            label = "speaker_tint"
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = speakerBg
                        ) {
                            IconButton(onClick = {
                                val t = tts
                                if (t != null) {
                                    if (isTtsSpeaking) {
                                        t.stop()
                                        isTtsSpeaking = false
                                    } else {
                                        t.setLanguage(detectedLang)
                                        t.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "article_utterance")
                                        isTtsSpeaking = true
                                    }
                                } else {
                                    Toast.makeText(context, ttsInitializing, Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(
                                    imageVector = if (isTtsSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(if (isTtsSpeaking) R.string.action_stop_tts else R.string.action_play_tts),
                                    tint = speakerTint
                                )
                            }
                        }

                        // 4. Summarize (AI)
                        val summarizeBg by animateColorAsState(
                            targetValue = if (isSummarizing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            label = "summarize_bg"
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = summarizeBg
                        ) {
                            IconButton(
                                onClick = onSummarize,
                                enabled = !isSummarizing
                            ) {
                                if (isSummarizing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = stringResource(R.string.action_summarize),
                                        tint = if (currentItem?.aiSummary != null) MaterialTheme.colorScheme.primary 
                                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // 5. WebView Toggle (Optional)
                        if (hasHtmlContent) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                IconButton(onClick = { useWebView = !useWebView }) {
                                    Icon(
                                        imageVector = if (useWebView) Icons.AutoMirrored.Filled.TextSnippet else Icons.Filled.Public,
                                        contentDescription = stringResource(if (useWebView) R.string.action_clean_view else R.string.action_web_view)
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (useWebView && hasHtmlContent) {
            HtmlWebView(
                htmlContent = content ?: description,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            )
        } else {
            CleanView(
                title = title,
                description = description,
                content = content,
                imageUrl = imageUrl,
                dominantColor = dominantColor,
                onDominantColorExtracted = { dominantColor = it },
                aiSummary = currentItem?.aiSummary,
                isSummarizing = isSummarizing,
                summarizationError = summarizationError,
                onSummarizeClick = onSummarize,
                readerFontSize = readerFontSize,
                readerFontFamilyName = readerFontFamilyName,
                readerLineHeight = readerLineHeight,
                readerPadding = readerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun CleanView(
    title: String,
    description: String,
    content: String?,
    imageUrl: String?,
    dominantColor: Color,
    onDominantColorExtracted: (Color) -> Unit,
    aiSummary: String?,
    isSummarizing: Boolean,
    summarizationError: String?,
    onSummarizeClick: () -> Unit,
    readerFontSize: Float,
    readerFontFamilyName: String,
    readerLineHeight: Float,
    readerPadding: Int,
    modifier: Modifier = Modifier
) {
    val fontFamily = when (readerFontFamilyName) {
        "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
        "SansSerif" -> androidx.compose.ui.text.font.FontFamily.SansSerif
        "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
        else -> androidx.compose.ui.text.font.FontFamily.Default
    }

    Column(modifier = modifier) {
        if (imageUrl != null) {
            DominantColorAsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 11f)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                onColorExtracted = onDominantColorExtracted
            )
        } else {
            Spacer(modifier = Modifier.statusBarsPadding().height(64.dp))
        }

        Column(
            modifier = Modifier
                .padding(horizontal = readerPadding.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = (readerFontSize * 1.5f).sp,
                    fontFamily = fontFamily
                ),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = (readerFontSize * 1.8f).sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            AiSummarySection(
                aiSummary = aiSummary,
                isSummarizing = isSummarizing,
                summarizationError = summarizationError,
                onSummarizeClick = onSummarizeClick,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (readerFontSize * 1.1f).sp,
                    fontFamily = fontFamily,
                    lineHeight = (readerFontSize * 1.1f * readerLineHeight).sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )

            if (content != null && content != description) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = readerFontSize.sp,
                        fontFamily = fontFamily,
                        lineHeight = (readerFontSize * readerLineHeight).sp
                    ),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

private enum class AiSummaryState { Idle, Loading, Loaded, Error }

@Composable
private fun AiSummarySection(
    aiSummary: String?,
    isSummarizing: Boolean,
    summarizationError: String?,
    onSummarizeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentState = when {
        isSummarizing -> AiSummaryState.Loading
        aiSummary != null -> AiSummaryState.Loaded
        summarizationError != null -> AiSummaryState.Error
        else -> AiSummaryState.Idle
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = 300f
                )
            )
    ) {
        AnimatedContent(
            targetState = currentState,
            transitionSpec = {
                when {
                    targetState == AiSummaryState.Loaded && initialState != AiSummaryState.Loaded ->
                        (expandVertically(expandFrom = Alignment.Top) + fadeIn(animationSpec = tween(300)))
                            .togetherWith(fadeOut(animationSpec = tween(200)))
                    else ->
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                }
            },
            label = "ai_summary"
        ) { state ->
            when (state) {
                AiSummaryState.Idle -> Spacer(Modifier.height(0.dp))
                AiSummaryState.Loading -> SummaryLoadingCard()
                AiSummaryState.Loaded -> SummaryLoadedCard(
                    summary = aiSummary ?: "",
                    onRegenerate = onSummarizeClick
                )
                AiSummaryState.Error -> SummaryErrorChip(
                    error = summarizationError ?: "",
                    onRetry = onSummarizeClick
                )
            }
        }
    }
}

@Composable
private fun SummaryLoadingCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerPosition = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_pos"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.ai_summary_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                repeat(4) { index ->
                    ShimmerLine(
                        widthFraction = when (index) {
                            0 -> 0.8f
                            3 -> 0.6f
                            else -> 1f
                        },
                        shimmerPosition = shimmerPosition.value
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ShimmerLine(
    widthFraction: Float,
    shimmerPosition: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(
                        -200f + shimmerPosition * 1400f,
                        0f
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        200f + shimmerPosition * 1400f,
                        0f
                    )
                )
            )
    )
}

@Composable
private fun SummaryLoadedCard(
    summary: String,
    onRegenerate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.ai_summary_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                RichText {
                    Markdown(content = summary)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onRegenerate) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.action_regenerate),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryErrorChip(
    error: String,
    onRetry: () -> Unit
) {
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(error) {
        shakeOffset.snapTo(0f)
        shakeOffset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 500
                0f at 0
                -8f at 50
                8f at 100
                -6f at 150
                6f at 200
                -3f at 250
                3f at 300
                0f at 500
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shakeOffset.value.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.action_retry),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HtmlWebView(
    htmlContent: String,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = false

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                        }
                    }

                    loadDataWithBaseURL(
                        null,
                        wrapHtml(htmlContent),
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

private fun wrapHtml(body: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: 'Roboto', sans-serif;
                    padding: 24px;
                    line-height: 1.6;
                    color: #1D1B20;
                    background-color: #FEF7FF;
                    font-size: 16px;
                }
                img { max-width: 100%; height: auto; border-radius: 20px; margin: 16px 0; }
                a { color: #6750A4; text-decoration: none; font-weight: bold; }
                pre { overflow-x: auto; background: #E6E0E9; padding: 16px; border-radius: 12px; }
                blockquote { border-left: 6px solid #6750A4; margin-left: 0; padding-left: 20px; color: #49454F; font-style: italic; }
                h1, h2, h3 { color: #1D1B20; font-weight: 900; }
            </style>
        </head>
        <body>$body</body>
        </html>
    """.trimIndent()
}
