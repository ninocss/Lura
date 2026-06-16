package com.nino161er.lura

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nino161er.rssfeed.data.notification.NotificationHelper
import com.nino161er.rssfeed.ui.RssViewModel
import com.nino161er.rssfeed.ui.navigation.AppNavigation
import com.nino161er.rssfeed.ui.theme.RSSFeedTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("rssfeed_settings", MODE_PRIVATE)
        val savedLang = prefs.getString("app_language", "") ?: ""
        val context = if (savedLang.isNotEmpty()) {
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(Locale.forLanguageTag(savedLang))
            newBase.createConfigurationContext(config)
        } else {
            newBase
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val viewModel: RssViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColorEnabled.collectAsState()
            val pitchBlack by viewModel.pitchBlack.collectAsState()
            RSSFeedTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor,
                pitchBlack = pitchBlack
            ) {
                val windowSizeClass = calculateWindowSizeClass(this)
                AppNavigation(
                    viewModel = viewModel,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }
    }
}
