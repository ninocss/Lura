package com.nino161er.rssfeed.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiSummarizer {
    suspend fun summarize(
        text: String,
        apiKey: String,
        model: String = "gemini-2.5-flash",
        language: String = "English",
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("API key is empty. Please configure it in Settings.")
        }
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Summarize this article concisely in $language. " +
                                "Keep it short as a summary, your text should be max. 30% of the article length. " +
                                "Highlight the key details in bullet points.\n\n" +
                                "Article text:\n$text")
                        })
                    })
                })
            })
        }

        conn.outputStream.use { os ->
            OutputStreamWriter(os, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }
        }

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = conn.inputStream.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            }
            val jsonResponse = JSONObject(responseText)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && (candidates.length() > 0)) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
            }
            throw Exception("Invalid response from Gemini.")
        } else {
            val errorText = conn.errorStream?.use { errorStream ->
                BufferedReader(InputStreamReader(errorStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: "Unknown error"
            throw Exception("Gemini error ($responseCode): $errorText")
        }
    }
}
