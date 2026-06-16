package com.nino161er.rssfeed.data.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalSummarizer : AiSummarizer {

    override suspend fun summarize(text: String, language: String): String {
        // LocalSummarizer requires Context to access the model file and engine.
        // The interface currently doesn't support passing context.
        throw UnsupportedOperationException("Use summarize(text, context, language) instead")
    }

    suspend fun summarize(
        text: String,
        context: Context,
        language: String = "English",
    ): String = withContext(Dispatchers.Default) {
        if (text.isBlank()) return@withContext ""

        if (!LocalModelManager.isModelAvailable(context)) {
            throw IllegalStateException("No local model found. Please import it in Settings.")
        }

        val engine = LocalAiEngine.getOrCreateEngine(context)
        val conversation = engine.createConversation()

        val prompt = buildString {
            append("Summarize this article concisely in $language. ")
            append("Keep it short as a summary, your text should be max. 30% of the article length. ")
            append("Highlight the key details in bullet points.\n\n")
            append("Article text:\n")
            append(text)
        }

        val resultBuilder = StringBuilder()
        conversation.sendMessageAsync(prompt).collect { token ->
            resultBuilder.append(token)
        }

        resultBuilder.toString().trim()
    }
}
