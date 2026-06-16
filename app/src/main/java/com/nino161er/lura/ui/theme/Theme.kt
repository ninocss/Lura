package com.nino161er.rssfeed.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalFeedColors = staticCompositionLocalOf { FeedColors() }

@Composable
fun RSSFeedTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = true,
    pitchBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkScheme
        else -> LightScheme
    }

    if (isDark && pitchBlack) {
        colorScheme = colorScheme.copy(
            surface = Color.Black,
            background = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainerHigh = Color(0xFF111111),
            surfaceContainerLowest = Color.Black,
            surfaceVariant = Color(0xFF111111),
            onSurface = Color.White,
            onBackground = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(LocalFeedColors provides FeedColors()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
