package com.nino161er.rssfeed.data.ai

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalAiEngine {

    private const val TAG = "LocalAiEngine"

    @Volatile
    private var engine: Engine? = null

    suspend fun getOrCreateEngine(context: Context): Engine = withContext(Dispatchers.IO) {
        engine?.let { return@withContext it }

        synchronized(this) {
            engine?.let { return@synchronized it }

            val prefs = context.getSharedPreferences("rssfeed_settings", Context.MODE_PRIVATE)
            val useGpu = prefs.getBoolean("use_gpu_ai", true)
            val modelPath = LocalModelManager.modelFile(context).absolutePath

            try {
                if (useGpu) {
                    Log.d(TAG, "Attempting to initialize engine with GPU...")
                    return@synchronized tryCreateEngine(modelPath, Backend.GPU())
                }
            } catch (e: Exception) {
                Log.e(TAG, "GPU initialization failed, falling back to CPU", e)
            }

            Log.d(TAG, "Initializing engine with CPU...")
            return@synchronized tryCreateEngine(modelPath, Backend.CPU())
        }
    }

    private fun tryCreateEngine(modelPath: String, backend: Backend): Engine {
        val config = EngineConfig(
            modelPath = modelPath,
            backend = backend,
        )
        return Engine(config).also {
            it.initialize()
            engine = it
        }
    }

    fun release() {
        engine?.close()
        engine = null
    }
}
