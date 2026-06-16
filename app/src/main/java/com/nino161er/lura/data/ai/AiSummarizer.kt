package com.nino161er.rssfeed.data.ai

enum class AiProvider {
    GEMINI, OPENAI, MISTRAL, OLLAMA, ON_DEVICE
}

interface AiSummarizer {
    suspend fun summarize(text: String, language: String): String
}
