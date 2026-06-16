package com.nino161er.rssfeed.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nino161er.rssfeed.data.RssRepository
import com.nino161er.rssfeed.data.local.AppDatabase
import com.nino161er.rssfeed.data.model.RssFeed
import com.nino161er.rssfeed.data.model.RssItem
import com.nino161er.rssfeed.data.notification.NotificationHelper
import com.nino161er.rssfeed.data.ai.AiProvider
import com.nino161er.rssfeed.data.ai.LocalAiEngine
import com.nino161er.rssfeed.data.audio.NoiseGenerator
import com.nino161er.rssfeed.data.model.RadioStation
import com.nino161er.rssfeed.data.radio.RadioBrowserService
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.nino161er.rssfeed.data.radio.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import android.content.ComponentName
import android.content.Context
import android.annotation.SuppressLint
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RssViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RssRepository
    private val prefs = getApplication<Application>().getSharedPreferences("rssfeed_settings", Context.MODE_PRIVATE)

    val allFeeds: StateFlow<List<RssFeed>>
    val allItems: StateFlow<List<RssItem>>
    val starredItems: StateFlow<List<RssItem>>

    private val _radioFrequency = MutableStateFlow(prefs.getFloat("radio_frequency", 98.8f))
    val radioFrequency: StateFlow<Float> = _radioFrequency.asStateFlow()

    private val _isPlayingRadio = MutableStateFlow(false)
    val isPlayingRadio: StateFlow<Boolean> = _isPlayingRadio.asStateFlow()

    private val _savedRadioStations = MutableStateFlow<List<RadioStation>>(emptyList())
    val savedRadioStations: StateFlow<List<RadioStation>> = _savedRadioStations.asStateFlow()

    private val _radioAutoTune = MutableStateFlow(prefs.getBoolean("radio_auto_tune", true))
    val radioAutoTune: StateFlow<Boolean> = _radioAutoTune.asStateFlow()

    private val _radioStaticVolume = MutableStateFlow(prefs.getFloat("radio_static_volume", 0.4f))
    val radioStaticVolume: StateFlow<Float> = _radioStaticVolume.asStateFlow()

    private val _radioAutoPlay = MutableStateFlow(prefs.getBoolean("radio_auto_play", false))
    val radioAutoPlay: StateFlow<Boolean> = _radioAutoPlay.asStateFlow()

    private val _nearbyStations = MutableStateFlow<List<RadioStation>>(RadioStation.defaultStations)
    val nearbyStations: StateFlow<List<RadioStation>> = _nearbyStations.asStateFlow()

    private val _isLoadingStations = MutableStateFlow(false)
    val isLoadingStations: StateFlow<Boolean> = _isLoadingStations.asStateFlow()

    private val _stationsLoaded = MutableStateFlow(false)
    val stationsLoaded: StateFlow<Boolean> = _stationsLoaded.asStateFlow()

    // News browser state
    private val _isNewsBrowserActive = MutableStateFlow(false)
    val isNewsBrowserActive: StateFlow<Boolean> = _isNewsBrowserActive.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun setNewsBrowserActive(active: Boolean) {
        _isNewsBrowserActive.value = active
    }

    // Custom newspaper sources
    private val _customNewsSources = MutableStateFlow<List<CustomNewsSource>>(loadCustomNewsSources())
    val customNewsSources: StateFlow<List<CustomNewsSource>> = _customNewsSources.asStateFlow()

    fun addCustomNewsSource(name: String, url: String) {
        val current = _customNewsSources.value.toMutableList()
        current.add(CustomNewsSource(name, url))
        _customNewsSources.value = current
        saveCustomNewsSources(current)
    }

    fun removeCustomNewsSource(index: Int) {
        val current = _customNewsSources.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _customNewsSources.value = current
            saveCustomNewsSources(current)
        }
    }

    private fun loadCustomNewsSources(): List<CustomNewsSource> {
        val json = prefs.getString("custom_news_sources", null) ?: return emptyList()
        return try {
            json.split("||").filter { it.isNotBlank() }.map { entry ->
                val parts = entry.split("::")
                CustomNewsSource(parts.getOrNull(0) ?: "", parts.getOrNull(1) ?: "")
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun saveCustomNewsSources(sources: List<CustomNewsSource>) {
        val json = sources.joinToString("||") { "${it.name}::${it.url}" }
        prefs.edit().putString("custom_news_sources", json).apply()
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(getApplication())
    }

    val noiseGenerator: NoiseGenerator by lazy { NoiseGenerator() }

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    val exoPlayer: Player?
        get() = controller

    init {
        val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture?.addListener({
            // Controller is ready
        }, MoreExecutors.directExecutor())
        
        val database = AppDatabase.getInstance(application)
        repository = RssRepository(database.rssFeedDao(), database.rssItemDao())

        allFeeds = repository.allFeeds.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allItems = repository.allItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        starredItems = repository.starredItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        try { noiseGenerator.release() } catch (_: Exception) {}
    }

    fun updateRadioFrequency(freq: Float) {
        _radioFrequency.value = freq
        prefs.edit().putFloat("radio_frequency", freq).apply()
    }

    fun toggleRadioPlayback(playing: Boolean) {
        _isPlayingRadio.value = playing
        try {
            if (playing) {
                controller?.play()
            } else {
                controller?.pause()
                noiseGenerator.stop()
            }
        } catch (_: Exception) {
        }
    }

    fun saveRadioStation(station: RadioStation) {
        val current = _savedRadioStations.value.toMutableList()
        if (!current.any { it.id == station.id }) {
            current.add(station)
            _savedRadioStations.value = current.sortedBy { it.frequency }
        }
    }

    fun removeRadioStation(station: RadioStation) {
        _savedRadioStations.value = _savedRadioStations.value.filter { it.id != station.id }
    }

    fun saveRadioAutoTune(enabled: Boolean) {
        prefs.edit().putBoolean("radio_auto_tune", enabled).apply()
        _radioAutoTune.value = enabled
    }

    fun saveRadioStaticVolume(volume: Float) {
        prefs.edit().putFloat("radio_static_volume", volume).apply()
        _radioStaticVolume.value = volume
        noiseGenerator.setVolume(volume)
    }

    fun saveRadioAutoPlay(enabled: Boolean) {
        prefs.edit().putBoolean("radio_auto_play", enabled).apply()
        _radioAutoPlay.value = enabled
    }

    fun playStationDirectly(station: RadioStation) {
        _radioFrequency.value = station.frequency
        _isPlayingRadio.value = true
        try {
            noiseGenerator.stop()
            controller?.let {
                val metadata = MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setSubtitle("${station.frequency} MHz")
                    .setArtist(station.category ?: "Radio")
                    .setArtworkUri(station.imageUrl?.let { Uri.parse(it) })
                    .build()
                
                val item = MediaItem.Builder()
                    .setMediaId(station.id)
                    .setUri(station.streamUrl)
                    .setMediaMetadata(metadata)
                    .build()
                    
                it.setMediaItem(item)
                it.prepare()
                it.volume = 1f
                it.play()
            }
        } catch (_: Exception) {
        }
    }

    @SuppressLint("MissingPermission")
    fun loadNearbyStations() {
        if (_isLoadingStations.value) return
        _isLoadingStations.value = true

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                viewModelScope.launch {
                    val result = RadioBrowserService.searchNearby(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    result.onSuccess { stations ->
                        if (stations.isNotEmpty()) {
                            _nearbyStations.value = stations
                            _stationsLoaded.value = true
                        }
                    }
                    _isLoadingStations.value = false
                }
            } else {
                // No location available, keep default stations
                _isLoadingStations.value = false
                _stationsLoaded.value = true
            }
        }.addOnFailureListener {
            // Location failed, keep default stations
            _isLoadingStations.value = false
            _stationsLoaded.value = true
        }
    }

    fun refreshFeeds() {
        viewModelScope.launch {
            repository.refreshFeeds()
        }
    }

    fun addFeed(url: String, category: String? = null) {
        viewModelScope.launch {
            try {
                repository.addFeed(url, category)
            } catch (e: Exception) {
                _error.value = "Invalid RSS feed or connection error"
                e.printStackTrace()
            }
        }
    }

    fun updateFeedCategory(feed: RssFeed, category: String?) {
        viewModelScope.launch {
            repository.updateFeedCategory(feed, category)
        }
    }

    fun deleteFeed(feed: RssFeed) {
        viewModelScope.launch {
            repository.deleteFeed(feed)
        }
    }

    fun toggleReadStatus(item: RssItem) {
        viewModelScope.launch {
            repository.updateItemReadStatus(item.id, !item.isRead)
        }
    }

    fun archiveItem(item: RssItem) {
        viewModelScope.launch {
            repository.updateItemArchivedStatus(item.id, true)
        }
    }

    fun deleteItem(item: RssItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun toggleStarredStatus(itemId: Long, isStarred: Boolean) {
        viewModelScope.launch {
            repository.updateItemStarredStatus(itemId, isStarred)
        }
    }

    fun markAsRead(itemId: Long) {
        viewModelScope.launch {
            repository.updateItemReadStatus(itemId, true)
        }
    }

    fun toggleMarkAllRead(feedId: Long, allRead: Boolean) {
        viewModelScope.launch {
            if (allRead) {
                repository.markAllFeedItemsAsUnread(feedId)
            } else {
                repository.markAllFeedItemsAsRead(feedId)
            }
        }
    }

    fun getGeminiApiKey(): String {
        val prefs = getApplication<Application>().getSharedPreferences("rssfeed_settings", Context.MODE_PRIVATE)
        return prefs.getString("gemini_api_key", "") ?: ""
    }

    fun saveGeminiApiKey(key: String) {
        val prefs = getApplication<Application>().getSharedPreferences("rssfeed_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("gemini_api_key", key).apply()
    }

    fun updateItemAiSummary(itemId: Long, summary: String?) {
        viewModelScope.launch {
            repository.updateItemAiSummary(itemId, summary)
        }
    }

    private val _aiProvider = MutableStateFlow(
        AiProvider.valueOf(prefs.getString("ai_provider", AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name)
    )
    val aiProvider: StateFlow<AiProvider> = _aiProvider.asStateFlow()

    fun getAiProvider(): AiProvider = _aiProvider.value

    fun saveAiProvider(provider: AiProvider) {
        prefs.edit().putString("ai_provider", provider.name).apply()
        _aiProvider.value = provider
    }

    fun getHfToken(): String = prefs.getString("hf_token", "") ?: ""

    fun saveHfToken(token: String) {
        prefs.edit().putString("hf_token", token).apply()
    }

    private val _useGpu = MutableStateFlow(prefs.getBoolean("use_gpu_ai", true))
    val useGpu: StateFlow<Boolean> = _useGpu.asStateFlow()

    fun saveUseGpu(enabled: Boolean) {
        prefs.edit().putBoolean("use_gpu_ai", enabled).apply()
        _useGpu.value = enabled
        LocalAiEngine.release() // Force engine recreation with new backend
    }

    private val _geminiModel = MutableStateFlow(prefs.getString("gemini_model", "gemini-2.5-flash") ?: "gemini-2.5-flash")
    val geminiModel: StateFlow<String> = _geminiModel.asStateFlow()

    fun getGeminiModel(): String = _geminiModel.value

    fun saveGeminiModel(model: String) {
        prefs.edit().putString("gemini_model", model).apply()
        _geminiModel.value = model
    }

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean("dynamic_color", true))
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    fun getThemeMode(): String = _themeMode.value

    fun saveThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun getDynamicColor(): Boolean = _dynamicColor.value

    fun saveDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean("dynamic_color", enabled).apply()
        _dynamicColor.value = enabled
    }

    private val _pitchBlack = MutableStateFlow(prefs.getBoolean("pitch_black", false))
    val pitchBlack: StateFlow<Boolean> = _pitchBlack.asStateFlow()

    fun savePitchBlack(enabled: Boolean) {
        prefs.edit().putBoolean("pitch_black", enabled).apply()
        _pitchBlack.value = enabled
    }

    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "") ?: "")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun getAppLanguage(): String = _appLanguage.value

    fun getAppLanguageName(): String {
        return when (_appLanguage.value) {
            "de" -> "German"
            "fr" -> "French"
            "es" -> "Spanish"
            "en" -> "English"
            else -> "the same language as the context"
        }
    }

    fun saveAppLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _appLanguage.value = lang
        
        // Apply the locale change to the app
        val appLocale: LocaleListCompat = if (lang.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(lang)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private val _hideReadArticles = MutableStateFlow(prefs.getBoolean("hide_read_articles", false))
    val hideReadArticles: StateFlow<Boolean> = _hideReadArticles.asStateFlow()

    fun getHideReadArticles(): Boolean = _hideReadArticles.value

    fun saveHideReadArticles(enabled: Boolean) {
        prefs.edit().putBoolean("hide_read_articles", enabled).apply()
        _hideReadArticles.value = enabled
    }

    private val _articleViewMode = MutableStateFlow(prefs.getString("article_view_mode", "clean") ?: "clean")
    val articleViewMode: StateFlow<String> = _articleViewMode.asStateFlow()

    fun getArticleViewMode(): String = _articleViewMode.value

    fun saveArticleViewMode(mode: String) {
        prefs.edit().putString("article_view_mode", mode).apply()
        _articleViewMode.value = mode
    }

    private val _refreshInterval = MutableStateFlow(prefs.getInt("refresh_interval", 60))
    val refreshInterval: StateFlow<Int> = _refreshInterval.asStateFlow()

    private val _onboardingComplete = MutableStateFlow(prefs.getBoolean("onboarding_complete", false))
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete.asStateFlow()

    fun getRefreshInterval(): Int = _refreshInterval.value

    fun saveRefreshInterval(minutes: Int) {
        prefs.edit().putInt("refresh_interval", minutes).apply()
        _refreshInterval.value = minutes
        NotificationHelper.scheduleRefresh(getApplication(), minutes)
    }

    fun isOnboardingComplete(): Boolean {
        return _onboardingComplete.value
    }

    fun setOnboardingComplete() {
        prefs.edit().putBoolean("onboarding_complete", true).apply()
        _onboardingComplete.value = true
    }

    private val _readerFontSize = MutableStateFlow(prefs.getFloat("reader_font_size", 16f))
    val readerFontSize: StateFlow<Float> = _readerFontSize.asStateFlow()

    fun saveReaderFontSize(size: Float) {
        prefs.edit().putFloat("reader_font_size", size).apply()
        _readerFontSize.value = size
    }

    private val _readerFontFamily = MutableStateFlow(prefs.getString("reader_font_family", "Serif") ?: "Serif")
    val readerFontFamily: StateFlow<String> = _readerFontFamily.asStateFlow()

    fun saveReaderFontFamily(family: String) {
        prefs.edit().putString("reader_font_family", family).apply()
        _readerFontFamily.value = family
    }

    private val _readerLineHeight = MutableStateFlow(prefs.getFloat("reader_line_height", 1.5f))
    val readerLineHeight: StateFlow<Float> = _readerLineHeight.asStateFlow()

    fun saveReaderLineHeight(height: Float) {
        prefs.edit().putFloat("reader_line_height", height).apply()
        _readerLineHeight.value = height
    }

    private val _readerHorizontalPadding = MutableStateFlow(prefs.getInt("reader_horizontal_padding", 16))
    val readerHorizontalPadding: StateFlow<Int> = _readerHorizontalPadding.asStateFlow()

    fun saveReaderHorizontalPadding(padding: Int) {
        prefs.edit().putInt("reader_horizontal_padding", padding).apply()
        _readerHorizontalPadding.value = padding
    }

    // RSS Settings
    private val _showArticleImages = MutableStateFlow(prefs.getBoolean("show_article_images", true))
    val showArticleImages: StateFlow<Boolean> = _showArticleImages.asStateFlow()

    fun saveShowArticleImages(show: Boolean) {
        prefs.edit().putBoolean("show_article_images", show).apply()
        _showArticleImages.value = show
    }

    private val _markReadOnScroll = MutableStateFlow(prefs.getBoolean("mark_read_on_scroll", false))
    val markReadOnScroll: StateFlow<Boolean> = _markReadOnScroll.asStateFlow()

    fun saveMarkReadOnScroll(enabled: Boolean) {
        prefs.edit().putBoolean("mark_read_on_scroll", enabled).apply()
        _markReadOnScroll.value = enabled
    }

    private val _defaultArticleView = MutableStateFlow(prefs.getString("default_article_view", "clean") ?: "clean")
    val defaultArticleView: StateFlow<String> = _defaultArticleView.asStateFlow()

    fun saveDefaultArticleView(view: String) {
        prefs.edit().putString("default_article_view", view).apply()
        _defaultArticleView.value = view
    }

    // News Settings
    private val _newsAutoRefresh = MutableStateFlow(prefs.getBoolean("news_auto_refresh", true))
    val newsAutoRefresh: StateFlow<Boolean> = _newsAutoRefresh.asStateFlow()

    fun saveNewsAutoRefresh(enabled: Boolean) {
        prefs.edit().putBoolean("news_auto_refresh", enabled).apply()
        _newsAutoRefresh.value = enabled
    }

    private val _newsDefaultView = MutableStateFlow(prefs.getString("news_default_view", "grid") ?: "grid")
    val newsDefaultView: StateFlow<String> = _newsDefaultView.asStateFlow()

    fun saveNewsDefaultView(view: String) {
        prefs.edit().putString("news_default_view", view).apply()
        _newsDefaultView.value = view
    }

    // More Radio Settings
    private val _radioSleepTimer = MutableStateFlow(prefs.getInt("radio_sleep_timer", 0))
    val radioSleepTimer: StateFlow<Int> = _radioSleepTimer.asStateFlow()

    fun saveRadioSleepTimer(minutes: Int) {
        prefs.edit().putInt("radio_sleep_timer", minutes).apply()
        _radioSleepTimer.value = minutes
    }

    private val _radioShowNotifications = MutableStateFlow(prefs.getBoolean("radio_show_notifications", true))
    val radioShowNotifications: StateFlow<Boolean> = _radioShowNotifications.asStateFlow()

    fun saveRadioShowNotifications(show: Boolean) {
        prefs.edit().putBoolean("radio_show_notifications", show).apply()
        _radioShowNotifications.value = show
    }

    private val _radioPreferredBand = MutableStateFlow(prefs.getString("radio_preferred_band", "all") ?: "all")
    val radioPreferredBand: StateFlow<String> = _radioPreferredBand.asStateFlow()

    fun saveRadioPreferredBand(band: String) {
        prefs.edit().putString("radio_preferred_band", band).apply()
        _radioPreferredBand.value = band
    }

    // More General Settings
    private val _showUnreadBadge = MutableStateFlow(prefs.getBoolean("show_unread_badge", true))
    val showUnreadBadge: StateFlow<Boolean> = _showUnreadBadge.asStateFlow()

    fun saveShowUnreadBadge(show: Boolean) {
        prefs.edit().putBoolean("show_unread_badge", show).apply()
        _showUnreadBadge.value = show
    }

    private val _compactMode = MutableStateFlow(prefs.getBoolean("compact_mode", false))
    val compactMode: StateFlow<Boolean> = _compactMode.asStateFlow()

    fun saveCompactMode(enabled: Boolean) {
        prefs.edit().putBoolean("compact_mode", enabled).apply()
        _compactMode.value = enabled
    }

    init {
        val savedInterval = prefs.getInt("refresh_interval", 60)
        NotificationHelper.scheduleRefresh(getApplication(), savedInterval)
    }
}

data class CustomNewsSource(val name: String, val url: String)
