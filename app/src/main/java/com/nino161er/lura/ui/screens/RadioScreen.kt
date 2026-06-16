package com.nino161er.rssfeed.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import com.nino161er.rssfeed.data.model.RadioStation
import com.nino161er.rssfeed.ui.RssViewModel
import com.nino161er.rssfeed.ui.navigation.Tab
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.Player
import androidx.media3.common.MediaMetadata
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    viewModel: RssViewModel,
    onOpenDrawer: () -> Unit,
    selectedSubTab: Tab = Tab.RadioTuner
) {
    when (selectedSubTab) {
        Tab.RadioSaved -> RadioSavedContent(viewModel, onOpenDrawer)
        else -> RadioTunerContent(viewModel, onOpenDrawer)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadioTunerContent(
    viewModel: RssViewModel,
    onOpenDrawer: () -> Unit
) {
    val frequency by viewModel.radioFrequency.collectAsState()
    val isPlaying by viewModel.isPlayingRadio.collectAsState()
    val staticVolume by viewModel.radioStaticVolume.collectAsState()
    val stations by viewModel.nearbyStations.collectAsState()
    val isLoading by viewModel.isLoadingStations.collectAsState()
    val stationsLoaded by viewModel.stationsLoaded.collectAsState()
    var currentStation by remember { mutableStateOf<RadioStation?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    val savedStations by viewModel.savedRadioStations.collectAsState()
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "saved_radio_effects")
    
    val activePulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "activePulse"
    )

    val exoPlayer = viewModel.exoPlayer
    val noiseGenerator = viewModel.noiseGenerator

    // Stream buffering state
    var isBuffering by remember { mutableStateOf(false) }
    DisposableEffect(exoPlayer) {
        if (exoPlayer == null) return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Request location permission only once on first launch
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.loadNearbyStations()
        }
    }

    LaunchedEffect(Unit) {
        if (!stationsLoaded) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Handle station tuning & noise playback (wrapped in try-catch for stability)
    LaunchedEffect(frequency, isPlaying, exoPlayer) {
        try {
            if (exoPlayer == null) return@LaunchedEffect
            val closest = stations.minByOrNull { abs(it.frequency - frequency) }
            val distance = closest?.let { abs(it.frequency - frequency) } ?: 10f

            if (distance < 0.15f && closest != null) {
                if (currentStation != closest) {
                    currentStation = closest
                    isSaved = savedStations.any { it.id == closest.id }
                    noiseGenerator.stop()
                    exoPlayer?.let {
                        val metadata = MediaMetadata.Builder()
                            .setTitle(closest.name)
                            .setSubtitle("${closest.frequency} MHz")
                            .setArtist(closest.category ?: "Radio")
                            .setArtworkUri(closest.imageUrl?.let { Uri.parse(it) })
                            .build()
                        
                        val item = MediaItem.Builder()
                            .setMediaId(closest.id)
                            .setUri(closest.streamUrl)
                            .setMediaMetadata(metadata)
                            .build()
                            
                        it.setMediaItem(item)
                        it.prepare()
                        it.volume = 1f
                        if (isPlaying) it.play()
                    }
                }
            } else {
                if (currentStation != null) {
                    currentStation = null
                    exoPlayer?.stop()
                }
                if (isPlaying) {
                    noiseGenerator.setVolume(staticVolume)
                    noiseGenerator.start()
                } else {
                    noiseGenerator.stop()
                }
            }
        } catch (_: Exception) {
            // ExoPlayer or NoiseGenerator may be released during config changes
        }
    }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                "Radio",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Large Frequency Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(140.dp)
            ) {
                if (isBuffering && isPlaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(180.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val frequencyString = String.format("%.1f", frequency)
                        frequencyString.forEachIndexed { index, char ->
                            AnimatedContent(
                                targetState = char,
                                transitionSpec = {
                                    val direction = if (targetState > initialState) 1 else -1
                                    (slideInVertically(
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                        initialOffsetY = { height -> direction * height / 2 }
                                    ) + fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f))
                                        .togetherWith(
                                            slideOutVertically(
                                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                                targetOffsetY = { height -> -direction * height / 2 }
                                            ) + fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.5f)
                                        ).using(SizeTransform(clip = false))
                                },
                                label = "digit_$index"
                            ) { targetChar ->
                                val blurValue by transition.animateFloat(
                                    transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
                                    label = "blur"
                                ) { state -> if (state == EnterExitState.Visible) 0f else 12f }

                                Text(
                                    text = targetChar.toString(),
                                    fontSize = 84.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = if (targetChar == '.') 0.sp else (-2).sp,
                                    modifier = Modifier.blur(blurValue.dp)
                                )
                            }
                        }
                    }
                    
                    val mhzAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "mhzAlpha"
                    )

                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Text(
                            text = "MHz",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.graphicsLayer { alpha = mhzAlpha }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Horizontal Frequency Wheel
            FrequencyWheel(
                frequency = frequency,
                stations = stations,
                onFrequencyChange = { 
                    if (abs(it - frequency) > 0.05f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    viewModel.updateRadioFrequency(it) 
                }
            )

            // Material You Expressive loading indicator
            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Finding nearby stations...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(if (isLoading) 48.dp else 64.dp))

            // Station Info Card
            val cardScale by animateFloatAsState(
                targetValue = if (currentStation != null) 1.02f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "cardScale"
            )

            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isPlaying && !isBuffering) 1.08f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                    },
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(
                    containerColor = animateColorAsState(
                        if (currentStation != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                        label = "cardColor"
                    ).value
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (currentStation != null) 8.dp else 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Pulse effect behind play button
                        if (isPlaying && !isBuffering) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                        alpha = (1.1f - pulseScale) * 2f
                                    }
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .size(68.dp)
                                .graphicsLayer {
                                    val s = if (isPlaying) 0.95f else 1f
                                    scaleX = s
                                    scaleY = s
                                },
                            shape = CircleShape,
                            color = animateColorAsState(
                                if (isPlaying) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                label = "buttonColor"
                            ).value,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleRadioPlayback(!isPlaying)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isBuffering && isPlaying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    AnimatedContent(
                                        targetState = isPlaying,
                                        transitionSpec = {
                                            scaleIn(animationSpec = spring(Spring.DampingRatioMediumBouncy)) togetherWith 
                                            scaleOut()
                                        },
                                        label = "playIcon"
                                    ) { playing ->
                                        Icon(
                                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(38.dp),
                                            tint = if (playing) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentStation?.name ?: "Static Noise",
                            transitionSpec = {
                                fadeIn() + slideInHorizontally { it / 2 } togetherWith 
                                fadeOut() + slideOutHorizontally { -it / 2 }
                            },
                            label = "stationName"
                        ) { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        AnimatedContent(
                            targetState = if (currentStation != null) {
                                "${currentStation?.category ?: "Radio"} \u2022 Signal Strong"
                            } else "Tune in to a station",
                            transitionSpec = {
                                fadeIn() + slideInVertically { it / 2 } togetherWith 
                                fadeOut() + slideOutVertically { -it / 2 }
                            },
                            label = "stationStatus"
                        ) { status ->
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentStation?.let { station ->
                                if (isSaved) {
                                    viewModel.removeRadioStation(station)
                                } else {
                                    viewModel.saveRadioStation(station)
                                }
                                isSaved = !isSaved
                            }
                        },
                        modifier = Modifier.graphicsLayer {
                            rotationZ = if (isSaved) 360f else 0f
                        }
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            modifier = Modifier.size(30.dp),
                            tint = animateColorAsState(
                                if (isSaved) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                label = "bookmarkColor"
                            ).value
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

@Composable
fun FrequencyWheel(
    frequency: Float,
    stations: List<RadioStation>,
    onFrequencyChange: (Float) -> Unit
) {
    val range = 87.5f..108.0f
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val newFreq = frequency - (delta / 120f)
                    onFrequencyChange(newFreq.coerceIn(range))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = width / 2f

            // Draw ticks
            for (i in -20..20) {
                val tickFreq = ((frequency * 10).roundToInt() + i) / 10f
                if (tickFreq in range) {
                    val x = center + (i * 30f)
                    val isMajor = (tickFreq * 10).roundToInt() % 10 == 0
                    val isHalfMajor = (tickFreq * 10).roundToInt() % 5 == 0
                    val isStation = stations.any { abs(it.frequency - tickFreq) < 0.05f }
                    val tickHeight = if (isStation) 50f else if (isMajor) 40f else if (isHalfMajor) 28f else 16f
                    val alpha = 1f - (abs(i) / 25f)
                    val tickColor = if (isStation) tertiary else onSurface
                    val strokeW = if (isStation) 6f else if (isMajor) 4f else 2f

                    drawLine(
                        color = tickColor.copy(alpha = alpha.coerceIn(0f, 1f)),
                        start = Offset(x, height / 2f - tickHeight / 2f),
                        end = Offset(x, height / 2f + tickHeight / 2f),
                        strokeWidth = strokeW
                    )
                }
            }

            // Center Indicator
            drawLine(
                color = primary,
                start = Offset(center, 10f),
                end = Offset(center, height - 10f),
                strokeWidth = 6f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadioSavedContent(
    viewModel: RssViewModel,
    onOpenDrawer: () -> Unit
) {
    val savedStations by viewModel.savedRadioStations.collectAsState()
    val isPlaying by viewModel.isPlayingRadio.collectAsState()
    val currentFreq by viewModel.radioFrequency.collectAsState()
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "saved_radio_effects")
    
    val activePulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "activePulse"
    )

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                "Saved Stations",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            if (savedStations.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No saved stations yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the bookmark icon on a station to save it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 180.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    savedStations.forEach { station ->
                        val isActive = abs(station.frequency - currentFreq) < 0.05f && isPlaying
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.playStationDirectly(station) 
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.PlayArrow else Icons.Default.RadioButtonChecked,
                                    contentDescription = null,
                                    tint = if (isActive) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .graphicsLayer {
                                            if (isActive) {
                                                scaleX = activePulse
                                                scaleY = activePulse
                                            }
                                        }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = station.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", station.frequency)} MHz" +
                                            (station.category?.let { " \u2022 $it" } ?: "") +
                                            (station.country?.let { " \u2022 $it" } ?: ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
                                    )
                                }
                                IconButton(onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.removeRadioStation(station) 
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
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
