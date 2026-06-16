package com.nino161er.rssfeed.data.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.palette.graphics.Palette
import java.io.IOException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ColorExtractor {

    suspend fun extractFromUrl(imageUrl: String): Int? = withContext(Dispatchers.IO) {
        try {
            val bitmap = URL(imageUrl).openStream().use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext null

            extractDominantColor(bitmap)
        } catch (e: IOException) {
            null
        }
    }

    fun extractDominantColor(bitmap: Bitmap): Int? {
        val palette = Palette.from(bitmap).generate()

        return palette.dominantSwatch?.rgb
            ?: palette.vibrantSwatch?.rgb
            ?: palette.mutedSwatch?.rgb
            ?: palette.darkVibrantSwatch?.rgb
            ?: palette.lightVibrantSwatch?.rgb
    }

    fun isLightColor(color: Int): Boolean {
        val luminance = (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255.0
        return luminance > 0.5
    }
}
