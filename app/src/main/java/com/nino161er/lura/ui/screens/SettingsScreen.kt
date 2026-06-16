package com.nino161er.rssfeed.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nino161er.rssfeed.R
import com.nino161er.rssfeed.data.ai.AiProvider
import com.nino161er.rssfeed.data.ai.LocalModelManager
import com.nino161er.rssfeed.ui.RssViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RssViewModel,
    onNavigateToManageFeeds: () -> Unit,
    onBack: () -> Unit,
    settingsContext: String = "general"
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        when (settingsContext) {
                            "radio" -> "Radio Settings"
                            "news" -> "News Settings"
                            "rss" -> "RSS Settings"
                            else -> "App Settings"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Context-specific settings
            if (settingsContext == "radio") {
                SettingsSectionHeader("Playback")
                RadioVolumeSetting(viewModel)
                RadioStaticVolumeSetting(viewModel)
                RadioAutoPlaySetting(viewModel)

                SettingsSectionHeader("Playback Controls")
                RadioPlayPauseSetting(viewModel)
                RadioSleepTimerSetting(viewModel)
                RadioPreferredBandSetting(viewModel)

                SettingsSectionHeader("Notifications")
                SwitchSetting(
                    title = "Show playback notifications",
                    subtitle = "Display notification while radio is playing",
                    icon = Icons.Default.Notifications,
                    checked = viewModel.radioShowNotifications.collectAsState().value,
                    onCheckedChange = { viewModel.saveRadioShowNotifications(it) }
                )

                SettingsSectionHeader("Info")
                RadioInfoSetting(viewModel)
                RadioSavedCountSetting(viewModel)
            } else if (settingsContext == "news") {
                SettingsSectionHeader("News")
                ActionSetting(
                    title = "Manage Newspapers",
                    subtitle = "Add or remove news sources",
                    icon = Icons.Default.Newspaper,
                    onClick = onNavigateToManageFeeds
                )
                NewsAutoRefreshSetting(viewModel)
                NewsDefaultViewSetting(viewModel)
            } else if (settingsContext == "rss") {
                // RSS-specific settings
                SettingsSectionHeader("Feeds")
                ActionSetting(
                    title = "Manage Feeds",
                    subtitle = "Add or remove RSS sources",
                    icon = Icons.Default.RssFeed,
                    onClick = onNavigateToManageFeeds
                )
                SwitchSetting(
                    title = "Hide read articles",
                    subtitle = "Only show unread items in the list",
                    icon = Icons.Default.VisibilityOff,
                    checked = viewModel.hideReadArticles.collectAsState().value,
                    onCheckedChange = { viewModel.saveHideReadArticles(it) }
                )
                RefreshIntervalSetting(viewModel)

                SettingsSectionHeader("Article Display")
                SwitchSetting(
                    title = "Show article images",
                    subtitle = "Load images in article lists",
                    icon = Icons.Default.Image,
                    checked = viewModel.showArticleImages.collectAsState().value,
                    onCheckedChange = { viewModel.saveShowArticleImages(it) }
                )
                DefaultArticleViewSetting(viewModel)
                SwitchSetting(
                    title = "Mark as read on scroll",
                    subtitle = "Automatically mark articles as read when scrolling past them",
                    icon = Icons.Default.Visibility,
                    checked = viewModel.markReadOnScroll.collectAsState().value,
                    onCheckedChange = { viewModel.saveMarkReadOnScroll(it) }
                )
            }

            // Always show general settings
            SettingsSectionHeader("Language")
            LanguageSetting(viewModel)

            SettingsSectionHeader("Appearance")
            ThemeSetting(viewModel)
            SwitchSetting(
                title = "Pitch black theme",
                subtitle = "Use pure black background in dark mode",
                icon = Icons.Default.BrightnessLow,
                checked = viewModel.pitchBlack.collectAsState().value,
                onCheckedChange = { viewModel.savePitchBlack(it) }
            )
            SwitchSetting(
                title = "Dynamic Color",
                subtitle = "Match system colors (Android 12+)",
                icon = Icons.Default.Palette,
                checked = viewModel.dynamicColorEnabled.collectAsState().value,
                onCheckedChange = { viewModel.saveDynamicColor(it) }
            )
            SwitchSetting(
                title = "Compact mode",
                subtitle = "Show more items with smaller spacing",
                icon = Icons.Default.ViewCompact,
                checked = viewModel.compactMode.collectAsState().value,
                onCheckedChange = { viewModel.saveCompactMode(it) }
            )

            SettingsSectionHeader("Reader")
            ReaderFontFamilySetting(viewModel)
            SliderSetting(
                title = "Font Size",
                value = viewModel.readerFontSize.collectAsState().value,
                onValueChange = { viewModel.saveReaderFontSize(it) },
                valueRange = 12f..32f,
                icon = Icons.Default.TextFields
            )
            SliderSetting(
                title = "Line Height",
                value = viewModel.readerLineHeight.collectAsState().value,
                onValueChange = { viewModel.saveReaderLineHeight(it) },
                valueRange = 1.0f..2.5f,
                icon = Icons.Default.LineWeight
            )
            SliderSetting(
                title = "Horizontal Padding",
                value = viewModel.readerHorizontalPadding.collectAsState().value.toFloat(),
                onValueChange = { viewModel.saveReaderHorizontalPadding(it.toInt()) },
                valueRange = 0f..48f,
                icon = Icons.AutoMirrored.Filled.FormatIndentIncrease
            )

            SettingsSectionHeader("AI Summarizer")
            AiProviderSetting(viewModel)
            
            val selectedProvider by viewModel.aiProvider.collectAsState()
            if (selectedProvider == AiProvider.GEMINI) {
                GeminiApiKeySetting(viewModel)
                GeminiModelSetting(viewModel)
            } else if (selectedProvider == AiProvider.ON_DEVICE) {
                HfTokenSetting(viewModel)
                SwitchSetting(
                    title = "GPU Acceleration",
                    subtitle = "Use GPU for faster summarization (disable if it crashes)",
                    icon = Icons.Default.Speed,
                    checked = viewModel.useGpu.collectAsState().value,
                    onCheckedChange = { viewModel.saveUseGpu(it) }
                )
                LocalAiModelSetting(viewModel)
            }

            SettingsSectionHeader("General")
            SwitchSetting(
                title = "Show unread badge",
                subtitle = "Display unread count badge on app icon",
                icon = Icons.Default.Notifications,
                checked = viewModel.showUnreadBadge.collectAsState().value,
                onCheckedChange = { viewModel.saveShowUnreadBadge(it) }
            )
            
            ActionSetting(
                title = "About",
                subtitle = "Lura v${getAppVersion(context)}",
                icon = Icons.Default.Info,
                onClick = { /* Show about dialog or screen */ }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun LanguageSetting(viewModel: RssViewModel) {
    val currentLang = viewModel.appLanguage.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val languages = listOf(
        "" to "System Default",
        "en" to "English",
        "de" to "Deutsch",
        "fr" to "Français",
        "es" to "Español"
    )

    SettingCard(
        title = "Language",
        subtitle = languages.find { it.first == currentLang }?.second ?: "System Default",
        icon = Icons.Default.Public,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveAppLanguage(code)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentLang == code, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ThemeSetting(viewModel: RssViewModel) {
    val currentTheme = viewModel.themeMode.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val themes = listOf(
        "system" to "System Default",
        "light" to "Light",
        "dark" to "Dark"
    )

    SettingCard(
        title = "Theme",
        subtitle = themes.find { it.first == currentTheme }?.second ?: "System Default",
        icon = Icons.Default.Palette,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    themes.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveThemeMode(code)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentTheme == code, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReaderFontFamilySetting(viewModel: RssViewModel) {
    val currentFont = viewModel.readerFontFamily.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val fonts = listOf("Serif", "SansSerif", "Monospace", "Default")

    SettingCard(
        title = "Font Family",
        subtitle = currentFont,
        icon = Icons.Default.FontDownload,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Font") },
            text = {
                Column {
                    fonts.forEach { font ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveReaderFontFamily(font)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentFont == font, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(font)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RefreshIntervalSetting(viewModel: RssViewModel) {
    val currentInterval = viewModel.refreshInterval.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val intervals = listOf(
        0 to "Manual",
        15 to "15 minutes",
        30 to "30 minutes",
        60 to "1 hour",
        120 to "2 hours",
        240 to "4 hours"
    )

    SettingCard(
        title = "Refresh Interval",
        subtitle = intervals.find { it.first == currentInterval }?.second ?: "1 hour",
        icon = Icons.Default.Refresh,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Refresh Interval") },
            text = {
                Column {
                    intervals.forEach { (min, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveRefreshInterval(min)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentInterval == min, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AiProviderSetting(viewModel: RssViewModel) {
    val currentProvider by viewModel.aiProvider.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val providers = listOf(
        AiProvider.GEMINI to "Gemini (Cloud)",
        AiProvider.ON_DEVICE to "On-Device (Offline)"
    )

    SettingCard(
        title = "AI Provider",
        subtitle = providers.find { it.first == currentProvider }?.second ?: "Gemini",
        icon = Icons.Default.SmartToy,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select AI Provider") },
            text = {
                Column {
                    providers.forEach { (provider, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveAiProvider(provider)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentProvider == provider, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HfTokenSetting(viewModel: RssViewModel) {
    var token by remember { mutableStateOf(viewModel.getHfToken()) }
    var visible by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current

    SettingCard(
        title = "Hugging Face Token",
        subtitle = if (token.isEmpty()) "Not set (Needed for Gated Models)" else "••••••••",
        icon = Icons.Default.Key,
        onClick = null
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("HF Access Token") },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        visible = !visible 
                    }) {
                        Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.saveHfToken(token) 
                })
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    uriHandler.openUri("https://huggingface.co/settings/tokens") 
                }) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Get Token")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveHfToken(token) 
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun LocalAiModelSetting(viewModel: RssViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var modelAvailable by remember { mutableStateOf(LocalModelManager.isModelAvailable(context)) }
    var isImporting by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isImporting = true
            scope.launch {
                val result = LocalModelManager.importModel(context, uri)
                if (result.isSuccess) {
                    modelAvailable = true
                    Toast.makeText(context, "Model imported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
                isImporting = false
            }
        }
    }

    SettingCard(
        title = "Local AI Model (.litertlm)",
        subtitle = if (modelAvailable)
            "Installed (${LocalModelManager.modelSizeMb(context)} MB)"
        else "No model found",
        icon = if (modelAvailable) Icons.Default.DownloadDone else Icons.Default.DownloadForOffline,
        onClick = { 
            if (!isImporting) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDownloadDialog = true 
            }
        }
    ) {
        if (isImporting) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                LinearProgressIndicator(
                    progress = { if (downloadProgress > 0) downloadProgress else 0f },
                    modifier = Modifier.fillMaxWidth()
                )
                if (downloadProgress > 0) {
                    Text(
                        "${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (modelAvailable) {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        LocalModelManager.deleteModel(context)
                        modelAvailable = false
                    }
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete Model", color = MaterialTheme.colorScheme.error)
                }
            } else if (!isImporting) {
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    pickerLauncher.launch(arrayOf("*/*")) 
                }) {
                    Icon(Icons.Default.FileOpen, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Import File")
                }
            }
        }
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = { Text("Download AI Model") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a model to download directly (requires internet):", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    LocalModelManager.availableModels.forEach { model ->
                        OutlinedCard(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDownloadDialog = false
                                isImporting = true
                                downloadProgress = 0f
                                val token = viewModel.getHfToken()
                                scope.launch {
                                    val result = LocalModelManager.downloadModel(context, model, token) {
                                        downloadProgress = it
                                    }
                                    if (result.isSuccess) {
                                        modelAvailable = true
                                        Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Download failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    }
                                    isImporting = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(model.name, fontWeight = FontWeight.Bold)
                                Text(model.description, style = MaterialTheme.typography.bodySmall)
                                Text("${model.sizeMb} MB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GeminiApiKeySetting(viewModel: RssViewModel) {
    var apiKey by remember { mutableStateOf(viewModel.getGeminiApiKey()) }
    var visible by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current

    SettingCard(
        title = "Gemini API Key",
        subtitle = if (apiKey.isEmpty()) "Not set" else "••••••••",
        icon = Icons.Default.Key,
        onClick = null
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        visible = !visible 
                    }) {
                        Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.saveGeminiApiKey(apiKey) 
                })
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    uriHandler.openUri("https://aistudio.google.com/app/apikey") 
                }) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Get Key")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveGeminiApiKey(apiKey) 
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun GeminiModelSetting(viewModel: RssViewModel) {
    val currentModel = viewModel.geminiModel.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val models = listOf(
        "gemini-2.5-flash" to "Flash (Fast)",
        "gemini-2.5-pro" to "Pro (Smart)",
        "gemini-2.0-flash" to "2.0 Flash"
    )

    SettingCard(
        title = "AI Model",
        subtitle = models.find { it.first == currentModel }?.second ?: "Flash",
        icon = Icons.Default.AutoAwesome,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select AI Model") },
            text = {
                Column {
                    models.forEach { (id, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveGeminiModel(id)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentModel == id, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: (() -> Unit)?,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick?.invoke()
        },
        enabled = onClick != null,
        interactionSource = interactionSource
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (onClick != null && content == null) {
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
            if (content != null) {
                content()
            }
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onCheckedChange(!checked) 
        },
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(it)
            })
        }
    }
}

@Composable
fun SliderSetting(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    icon: ImageVector,
    subtitle: String? = null,
    valueDisplay: (Float) -> String = { if (it % 1 == 0f) it.toInt().toString() else "%.1f".format(it) }
) {
    val haptic = LocalHapticFeedback.current
    var isInteracting by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isInteracting) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    
    val iconRotation by animateFloatAsState(
        targetValue = if (isInteracting) {
            val range = valueRange.endInclusive - valueRange.start
            if (range > 0) ((value - valueRange.start) / range - 0.5f) * 45f else 0f
        } else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "iconRotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (isInteracting) 6.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer {
                            rotationZ = iconRotation
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = valueDisplay(value),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.graphicsLayer {
                        val textScale = if (isInteracting) 1.2f else 1f
                        scaleX = textScale
                        scaleY = textScale
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            Slider(
                value = value,
                onValueChange = {
                    isInteracting = true
                    if (kotlin.math.abs(it - value) > (valueRange.endInclusive - valueRange.start) / 20f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onValueChange(it)
                },
                onValueChangeFinished = {
                    isInteracting = false
                },
                valueRange = valueRange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ActionSetting(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

fun getAppVersion(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (_: PackageManager.NameNotFoundException) {
        "1.0"
    }
}

@Composable
fun RadioVolumeSetting(viewModel: RssViewModel) {
    var volume by remember { mutableStateOf(0.8f) }
    
    SliderSetting(
        title = "Volume",
        value = volume,
        onValueChange = { vol ->
            volume = vol
            viewModel.exoPlayer?.volume = vol
            viewModel.noiseGenerator.setVolume(vol * 0.5f)
        },
        valueRange = 0f..1f,
        icon = if (volume == 0f) Icons.AutoMirrored.Filled.VolumeOff else if (volume < 0.5f) Icons.AutoMirrored.Filled.VolumeDown else Icons.AutoMirrored.Filled.VolumeUp,
        valueDisplay = { "${(it * 100).toInt()}%" }
    )
}

@Composable
fun RadioStaticVolumeSetting(viewModel: RssViewModel) {
    val staticVolume by viewModel.radioStaticVolume.collectAsState()

    SliderSetting(
        title = "Static Noise Volume",
        subtitle = "Volume of white noise between stations",
        value = staticVolume,
        onValueChange = { viewModel.saveRadioStaticVolume(it) },
        valueRange = 0f..1f,
        icon = Icons.Default.GraphicEq,
        valueDisplay = { "${(it * 100).toInt()}%" }
    )
}

@Composable
fun RadioAutoTuneSetting(viewModel: RssViewModel) {
    val autoTune by viewModel.radioAutoTune.collectAsState()

    SwitchSetting(
        title = "Auto-Tune",
        subtitle = "Snap to nearest station when close",
        icon = Icons.Default.Tune,
        checked = autoTune,
        onCheckedChange = { viewModel.saveRadioAutoTune(it) }
    )
}

@Composable
fun RadioAutoPlaySetting(viewModel: RssViewModel) {
    val autoPlay by viewModel.radioAutoPlay.collectAsState()

    SwitchSetting(
        title = "Auto-Play",
        subtitle = "Start playback when tuning into a station",
        icon = Icons.Default.PlayCircle,
        checked = autoPlay,
        onCheckedChange = { viewModel.saveRadioAutoPlay(it) }
    )
}

@Composable
fun RadioPlayPauseSetting(viewModel: RssViewModel) {
    val isPlaying by viewModel.isPlayingRadio.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 0.8f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        color = animateColorAsState(
            if (isPlaying) MaterialTheme.colorScheme.errorContainer.copy(alpha = pulseAlpha)
            else MaterialTheme.colorScheme.primaryContainer,
            label = "color"
        ).value,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.toggleRadioPlayback(!isPlaying) 
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = if (isPlaying) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (isPlaying) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isPlaying) "Stop Radio" else "Start Radio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPlaying) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun RadioInfoSetting(viewModel: RssViewModel) {
    val stations by viewModel.nearbyStations.collectAsState()
    val isLoading by viewModel.isLoadingStations.collectAsState()
    val haptic = LocalHapticFeedback.current

    SettingCard(
        title = "Nearby Stations",
        subtitle = if (isLoading) "Searching..." else "${stations.size} stations found near you",
        icon = Icons.Default.LocationOn,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.loadNearbyStations() 
        }
    )
}

@Composable
fun RadioSavedCountSetting(viewModel: RssViewModel) {
    val savedStations by viewModel.savedRadioStations.collectAsState()

    SettingCard(
        title = "Saved Stations",
        subtitle = "${savedStations.size} station${if (savedStations.size != 1) "s" else ""} saved",
        icon = Icons.Default.Bookmark,
        onClick = null
    )
}

@Composable
fun DefaultArticleViewSetting(viewModel: RssViewModel) {
    val currentView = viewModel.defaultArticleView.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val views = listOf(
        "clean" to "Clean View",
        "webview" to "WebView"
    )

    SettingCard(
        title = "Default Article View",
        subtitle = views.find { it.first == currentView }?.second ?: "Clean View",
        icon = Icons.AutoMirrored.Filled.Article,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Default View") },
            text = {
                Column {
                    views.forEach { (value, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveDefaultArticleView(value)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentView == value, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NewsAutoRefreshSetting(viewModel: RssViewModel) {
    SwitchSetting(
        title = "Auto-refresh articles",
        subtitle = "Automatically refresh news when opened",
        icon = Icons.Default.Refresh,
        checked = viewModel.newsAutoRefresh.collectAsState().value,
        onCheckedChange = { viewModel.saveNewsAutoRefresh(it) }
    )
}

@Composable
fun NewsDefaultViewSetting(viewModel: RssViewModel) {
    val currentView = viewModel.newsDefaultView.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val views = listOf(
        "grid" to "Grid View",
        "list" to "List View"
    )

    SettingCard(
        title = "Default News View",
        subtitle = views.find { it.first == currentView }?.second ?: "Grid View",
        icon = Icons.Default.GridView,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select News View") },
            text = {
                Column {
                    views.forEach { (value, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveNewsDefaultView(value)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentView == value, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RadioSleepTimerSetting(viewModel: RssViewModel) {
    val currentMinutes = viewModel.radioSleepTimer.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val options = listOf(
        0 to "Off",
        15 to "15 minutes",
        30 to "30 minutes",
        45 to "45 minutes",
        60 to "1 hour",
        90 to "1.5 hours",
        120 to "2 hours"
    )

    SettingCard(
        title = "Sleep Timer",
        subtitle = if (currentMinutes == 0) "Off" else "$currentMinutes minutes",
        icon = Icons.Default.Alarm,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Sleep Timer") },
            text = {
                Column {
                    options.forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveRadioSleepTimer(minutes)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentMinutes == minutes, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RadioPreferredBandSetting(viewModel: RssViewModel) {
    val currentBand = viewModel.radioPreferredBand.collectAsState().value
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val bands = listOf(
        "all" to "All Bands",
        "fm" to "FM Only (87.5-108 MHz)",
        "lw" to "Longwave (153-279 kHz)",
        "mw" to "Mediumwave (531-1602 kHz)"
    )

    SettingCard(
        title = "Preferred Band",
        subtitle = bands.find { it.first == currentBand }?.second ?: "All Bands",
        icon = Icons.Default.Tune,
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Band") },
            text = {
                Column {
                    bands.forEach { (value, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.saveRadioPreferredBand(value)
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentBand == value, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
