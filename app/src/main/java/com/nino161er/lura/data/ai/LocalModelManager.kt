package com.nino161er.rssfeed.data.ai

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class AiModel(
    val id: String,
    val name: String,
    val description: String,
    val sizeMb: Int,
    val url: String,
)

object LocalModelManager {

    private const val MODEL_FILENAME = "local_model.litertlm"

    val availableModels = listOf(
        AiModel(
            id = "gemma3_1b",
            name = "Gemma 3 1B-IT",
            description = "Fast & lightweight. Best for most devices. (int4)",
            sizeMb = 810,
            url = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm"
        ),
        AiModel(
            id = "gemma2_2b",
            name = "Gemma 2 2B-IT",
            description = "Higher quality, needs more RAM (min. 6GB). (int4)",
            sizeMb = 1600,
            url = "https://huggingface.co/litert-community/gemma-2-2b-it-litert-lm/resolve/main/gemma-2-2b-it.litertlm"
        ),
        AiModel(
            id = "phi3_mini",
            name = "Phi-3.5 Mini-IT",
            description = "Microsoft's capable small model. (int4)",
            sizeMb = 2200,
            url = "https://huggingface.co/litert-community/phi-3-5-mini-instruct-litert-lm/resolve/main/phi-3-5-mini-instruct-multi-prefill-seq-q4-ekv4096.litertlm"
        )
    )

    fun modelFile(context: Context): File =
        File(File(context.filesDir, "models").apply { mkdirs() }, MODEL_FILENAME)

    fun isModelAvailable(context: Context): Boolean =
        modelFile(context).exists() && (modelFile(context).length() > 0)

    fun modelSizeMb(context: Context): Long =
        modelFile(context).length() / (1024 * 1024)

    suspend fun downloadModel(
        context: Context,
        model: AiModel,
        hfToken: String? = null,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val target = modelFile(context)
            var currentUrl = model.url
            var connection: HttpURLConnection
            var redirectCount = 0
            val maxRedirects = 5

            while (true) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                if (!hfToken.isNullOrBlank()) {
                    connection.setRequestProperty("Authorization", "Bearer $hfToken")
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_SEE_OTHER ||
                    status == 307 || status == 308) {

                    if (redirectCount >= maxRedirects) {
                        return@withContext Result.failure(Exception("Too many redirects"))
                    }
                    currentUrl = connection.getHeaderField("Location")
                    redirectCount++
                    connection.disconnect()
                    continue
                }

                if (status != HttpURLConnection.HTTP_OK) {
                    val errorMsg = when(status) {
                        401 -> "Unauthorized: Check HF Token"
                        403 -> "Forbidden: You may need to accept the model terms on Hugging Face"
                        404 -> "Model file not found"
                        else -> "Server error: $status"
                    }
                    return@withContext Result.failure(Exception(errorMsg))
                }
                break
            }

            val totalSize = connection.contentLength.toLong()
            connection.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead: Long = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (totalSize > 0) {
                            onProgress(totalBytesRead.toFloat() / totalSize)
                        }
                    }
                }
            }
            connection.disconnect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importModel(context: Context, sourceUri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val target = modelFile(context)
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext Result.failure(Exception("Could not read file"))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun deleteModel(context: Context) {
        modelFile(context).delete()
        LocalAiEngine.release()
    }
}
