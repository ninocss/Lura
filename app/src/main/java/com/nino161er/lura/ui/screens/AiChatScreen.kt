package com.nino161er.rssfeed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nino161er.rssfeed.data.ai.AiProvider
import com.nino161er.rssfeed.data.ai.GeminiSummarizer
import com.nino161er.rssfeed.data.ai.LocalSummarizer
import com.nino161er.rssfeed.ui.RssViewModel
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiChatScreen(viewModel: RssViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allItems by viewModel.allItems.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }

    if (messages.isEmpty()) {
        messages.add(ChatMessage("Hello! I'm Lura, your AI assistant. I can summarize your news, find trends, or answer questions about your feeds. How can I help you today?", false))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat History
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        // Input Field
        Surface(
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Lura...") },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val userText = inputText
                        if (userText.isNotBlank()) {
                            messages.add(ChatMessage(userText, true))
                            inputText = ""
                            isLoading = true
                            
                            scope.launch {
                                try {
                                    val contextText = allItems.take(10).joinToString("\n---\n") { 
                                        "${it.title}: ${it.description ?: ""}" 
                                    }
                                    val provider = viewModel.getAiProvider()
                                    val language = viewModel.getAppLanguageName()
                                    val response = when (provider) {
                                        AiProvider.GEMINI -> {
                                            GeminiSummarizer.summarize(
                                                text = "Context from recent news:\n$contextText\n\nUser Question: $userText",
                                                apiKey = viewModel.getGeminiApiKey(),
                                                model = viewModel.getGeminiModel(),
                                                language = language
                                            )
                                        }
                                        AiProvider.ON_DEVICE -> {
                                            LocalSummarizer.summarize(
                                                text = "Context from recent news:\n$contextText\n\nUser Question: $userText",
                                                context = context,
                                                language = language
                                            )
                                        }
                                        else -> "Provider not yet supported for chat."
                                    }
                                    messages.add(ChatMessage(response, false))
                                } catch (e: Exception) {
                                    messages.add(ChatMessage("Error: ${e.message}", false))
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isUser) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Assistant, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = color,
                shape = shape,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (message.isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}
