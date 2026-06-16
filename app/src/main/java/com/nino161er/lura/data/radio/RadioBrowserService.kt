package com.nino161er.rssfeed.data.radio

import com.nino161er.rssfeed.data.model.RadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Client for the free Radio Browser API (https://api.radio-browser.info/).
 * No API key required. Searches for radio stations near a given GPS coordinate.
 */
object RadioBrowserService {

    // Multiple servers for fallback
    private val servers = listOf(
        "de1.api.radio-browser.info",
        "nl1.api.radio-browser.info",
        "at1.api.radio-browser.info"
    )

    /**
     * Search for radio stations near the given coordinates.
     * Returns stations sorted by popularity (click count), assigned to FM frequencies.
     */
    suspend fun searchNearby(
        latitude: Double,
        longitude: Double,
        limit: Int = 50,
        radiusKm: Int = 100
    ): Result<List<RadioStation>> = withContext(Dispatchers.IO) {
        try {
            val server = servers[0]
            val urlStr = buildString {
                append("https://$server/json/stations/search?")
                append("geo_lat=${latitude}")
                append("&geo_long=${longitude}")
                append("&geo_distance=${radiusKm * 1000}") // meters
                append("&limit=$limit")
                append("&order=clickcount")
                append("&reverse=true")
                append("&hidebroken=true")
                append("&has_geo_info=true")
            }

            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "Lura RSS Reader/1.0")

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                return@withContext Result.failure(Exception("API returned $responseCode"))
            }

            val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val jsonArray = JSONArray(jsonStr)
            val rawStations = mutableListOf<RawStation>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.optString("name", "").trim()
                val streamUrl = obj.optString("url_resolved", "").trim()
                val lat = obj.optDouble("geo_lat", Double.NaN)
                val lng = obj.optDouble("geo_long", Double.NaN)
                val country = obj.optString("country", "").trim().takeIf { it.isNotBlank() }
                val tags = obj.optString("tags", "").trim()
                val favicon = obj.optString("favicon", "").trim().takeIf { it.isNotBlank() }

                if (name.isNotBlank() && streamUrl.isNotBlank() &&
                    !lat.isNaN() && !lng.isNaN()) {
                    val category = extractCategory(tags)
                    rawStations.add(RawStation(name, streamUrl, lat, lng, country, category, favicon))
                }
            }

            // Sort by distance from user
            val sorted = rawStations.sortedBy { s ->
                haversineDistance(latitude, longitude, s.lat, s.lng)
            }

            // Assign FM frequencies across 87.5–108.0 MHz band
            val fmStations = assignFrequencies(sorted, 87.5f, 108.0f)

            Result.success(fmStations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Assigns FM frequencies to stations, spreading them evenly across the band.
     * Closer stations get frequencies in the lower/middle range, farther ones get upper range.
     */
    private fun assignFrequencies(
        stations: List<RawStation>,
        minFreq: Float,
        maxFreq: Float
    ): List<RadioStation> {
        if (stations.isEmpty()) return emptyList()

        val count = stations.size.coerceAtMost(53) // max stations on dial
        val step = (maxFreq - minFreq) / count

        return stations.take(count).mapIndexed { index, raw ->
            val freq = minFreq + (index * step)
            // Round to nearest 0.1
            val roundedFreq = Math.round(freq * 10) / 10f

            RadioStation(
                id = "rb_$index",
                name = raw.name,
                streamUrl = raw.streamUrl,
                frequency = roundedFreq,
                latitude = raw.lat,
                longitude = raw.lng,
                category = raw.category,
                imageUrl = raw.favicon,
                country = raw.country
            )
        }.sortedBy { it.frequency }
    }

    private fun extractCategory(tags: String): String? {
        if (tags.isBlank()) return null
        val tagList = tags.split(",").map { it.trim().lowercase() }
        val knownCategories = listOf(
            "pop", "rock", "jazz", "classical", "news", "talk",
            "electronic", "hip hop", "country", "dance", "house",
            "techno", "ambient", "chillout", "oldies", "80s", "90s",
            "reggae", "blues", "folk", "metal", "indie", "rnb",
            "soul", "funk", "latin", "world", "religious"
        )
        return tagList.firstOrNull { tag ->
            knownCategories.any { cat -> tag.contains(cat) }
        }?.replaceFirstChar { it.uppercase() }
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3
        val phi1 = lat1 * Math.PI / 180
        val phi2 = lat2 * Math.PI / 180
        val deltaPhi = (lat2 - lat1) * Math.PI / 180
        val deltaLambda = (lon2 - lon1) * Math.PI / 180
        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private data class RawStation(
        val name: String,
        val streamUrl: String,
        val lat: Double,
        val lng: Double,
        val country: String?,
        val category: String?,
        val favicon: String?
    )
}
