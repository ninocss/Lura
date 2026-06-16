package com.nino161er.rssfeed.ui.screens

import androidx.activity.ComponentActivity
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.nino161er.rssfeed.R
import com.nino161er.rssfeed.ui.RssViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: RssViewModel,
    onComplete: () -> Unit
) {
    val totalPages = 5
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }
    val selectedFeeds = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        apiKeyInput = viewModel.getGeminiApiKey()
    }

    val curatedFeeds = remember {
        listOf(
            CategoryFeed("News", listOf(
                "https://feeds.bbci.co.uk/news/rss.xml" to "BBC News",
                "https://www.theguardian.com/world/rss" to "The Guardian"
            )),
            CategoryFeed("Tech", listOf(
                "https://feeds.feedburner.com/TechCrunch" to "TechCrunch",
                "https://feeds.arstechnica.com/arstechnica/index" to "Ars Technica",
                "https://www.wired.com/feed/rss" to "Wired"
            )),
            CategoryFeed("Science", listOf(
                "https://www.nature.com/nature.rss" to "Nature",
                "https://www.sciencedaily.com/rss/all.xml" to "ScienceDaily"
            )),
            CategoryFeed("World", listOf(
                "https://www.aljazeera.com/xml/rss/all.xml" to "Al Jazeera",
                "https://rss.dw.com/rdf/rss-en-all" to "DW"
            ))
        )
    }

    val progress by animateFloatAsState(
        targetValue = (pagerState.currentPage + 1).toFloat() / totalPages,
        animationSpec = tween(500),
        label = "progress"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(Modifier.height(32.dp))

            // Modern Progress Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(horizontal = 32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false,
                beyondViewportPageCount = 1
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(48.dp))
                    when (page) {
                        0 -> WelcomePage()
                        1 -> AddSourcesPage(curatedFeeds, selectedFeeds)
                        2 -> AiSetupPage(apiKeyInput, { apiKeyInput = it }, keyVisible, { keyVisible = !keyVisible })
                        3 -> AppearancePage(viewModel)
                        4 -> AllSetPage()
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }

            // Navigation Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(totalPages) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = tween(300),
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(height = 8.dp, width = width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                if (pagerState.currentPage < totalPages - 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onComplete,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text("Skip", style = MaterialTheme.typography.titleMedium)
                        }
                        Button(
                            onClick = {
                                if (pagerState.currentPage == 1) {
                                    selectedFeeds.forEach { url -> viewModel.addFeed(url) }
                                }
                                if (pagerState.currentPage == 2 && apiKeyInput.isNotBlank()) {
                                    viewModel.saveGeminiApiKey(apiKeyInput.trim())
                                }
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text("Next", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    val onboardingActivity = LocalContext.current as? ComponentActivity
                    Button(
                        onClick = {
                            viewModel.saveGeminiApiKey(apiKeyInput.trim())
                            viewModel.setOnboardingComplete()
                            onboardingActivity?.recreate()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Get Started", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Icon(
        Icons.Default.AutoAwesome,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(Modifier.height(48.dp))

    Text(
        text = "Welcome to Lura",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(Modifier.height(16.dp))

    Text(
        text = "Follow your favorite sources, discover new voices, and get AI-powered summaries of every article.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

data class CategoryFeed(val category: String, val feeds: List<Pair<String, String>>)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddSourcesPage(
    curatedFeeds: List<CategoryFeed>,
    selectedFeeds: MutableList<String>
) {
    Text(
        text = "Add Sources",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = "Pick some feeds to get started. You can always add more later.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(32.dp))

    var customUrl by remember { mutableStateOf("") }
    OutlinedTextField(
        value = customUrl,
        onValueChange = { customUrl = it },
        label = { Text("Custom feed URL") },
        placeholder = { Text("https://example.com/feed.xml") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                if (customUrl.isNotBlank() && customUrl !in selectedFeeds) {
                    selectedFeeds.add(customUrl.trim())
                    customUrl = ""
                }
            }
        ),
        trailingIcon = {
            IconButton(
                onClick = {
                    if (customUrl.isNotBlank() && customUrl !in selectedFeeds) {
                        selectedFeeds.add(customUrl.trim())
                        customUrl = ""
                    }
                },
                enabled = customUrl.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add feed")
            }
        }
    )

    Spacer(Modifier.height(24.dp))

    curatedFeeds.forEach { categoryFeed ->
        Text(
            text = categoryFeed.category,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 4.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryFeed.feeds.forEach { (url, name) ->
                val isSelected = url in selectedFeeds
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedFeeds.remove(url) else selectedFeeds.add(url)
                    },
                    label = { Text(name) },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AiSetupPage(
    apiKeyInput: String,
    onApiKeyChange: (String) -> Unit,
    keyVisible: Boolean,
    onToggleKeyVisibility: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    
    Icon(
        Icons.Default.AutoAwesome, 
        null, 
        modifier = Modifier.size(80.dp), 
        tint = MaterialTheme.colorScheme.tertiary
    )

    Spacer(Modifier.height(32.dp))

    Text(
        text = "AI Summaries",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = "Get instant AI summaries of any article with Gemini. You can set this up now or later in Settings.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(32.dp))

    OutlinedTextField(
        value = apiKeyInput,
        onValueChange = onApiKeyChange,
        label = { Text("Gemini API Key") },
        placeholder = { Text("AIzaSy...") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Key, null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            IconButton(onClick = onToggleKeyVisibility) {
                Icon(
                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (keyVisible) "Hide key" else "Show key"
                )
            }
        },
        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
    )

    Spacer(Modifier.height(16.dp))

    OutlinedButton(
        onClick = { uriHandler.openUri("https://aistudio.google.com/app/apikey") },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Get an API Key")
    }

    Spacer(Modifier.height(24.dp))

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "You can skip this and set it up later in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearancePage(viewModel: RssViewModel) {
    Text(
        text = "Personalize",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(32.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            val currentTheme by viewModel.themeMode.collectAsState()
            val themeOptions = listOf("light", "system", "dark")
            val themeLabels = listOf(
                stringResource(R.string.settings_theme_light),
                stringResource(R.string.settings_theme_system),
                stringResource(R.string.settings_theme_dark)
            )
            val themeIcons = listOf(Icons.Default.LightMode, null, Icons.Default.DarkMode)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                themeOptions.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = currentTheme == option,
                        onClick = { viewModel.saveThemeMode(option) },
                        shape = SegmentedButtonDefaults.itemShape(index, themeOptions.size),
                        icon = {
                            if (themeIcons[index] != null) {
                                Icon(themeIcons[index]!!, null, Modifier.size(18.dp))
                            }
                        }
                    ) {
                        Text(themeLabels[index])
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_dynamic_color),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.settings_dynamic_color_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val dynamicColor by viewModel.dynamicColorEnabled.collectAsState()
                Switch(
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.saveDynamicColor(it) }
                )
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.settings_language),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
    )

    Spacer(Modifier.height(12.dp))

    val languages = listOf(
        "" to stringResource(R.string.settings_lang_system),
        "en" to stringResource(R.string.settings_lang_en),
        "de" to stringResource(R.string.settings_lang_de),
        "fr" to stringResource(R.string.settings_lang_fr),
        "es" to stringResource(R.string.settings_lang_es)
    )
    val currentLang by viewModel.appLanguage.collectAsState()
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        languages.forEach { (code, label) ->
            val isSelected = currentLang == code
            Surface(
                onClick = { viewModel.saveAppLanguage(code) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AllSetPage() {
    Icon(
        Icons.Default.Check,
        contentDescription = null,
        modifier = Modifier.size(100.dp),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(Modifier.height(48.dp))

    Text(
        text = "All Set!",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(16.dp))

    Text(
        text = "You're ready to start reading. Your feeds are waiting for you.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}
