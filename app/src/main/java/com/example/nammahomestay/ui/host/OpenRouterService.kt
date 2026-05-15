package com.example.nammahomestay.ui.host

import android.app.Activity
import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenRouterService(private val context: Context) {

    private const val API_KEY = "YOUR_OPENROUTER_API_KEY"
    // High-reliability FREE models. Gemini 2.0 and Llama 3.2 are currently the most stable.
    private val modelList = listOf(
        "google/gemini-2.0-flash-exp:free",
        "meta-llama/llama-3.2-3b-instruct:free",
        "mistralai/mistral-7b-instruct:free",
        "google/gemma-2-9b-it:free",
        "qwen/qwen-2-7b-instruct:free"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(40, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()

    fun generateDescription(prompt: String, callback: (String) -> Unit) {
        tryModel(prompt, 0, callback)
    }

    private fun tryModel(prompt: String, index: Int, callback: (String) -> Unit) {
        if (index >= modelList.size) {
            handleCallback(callback, "AI is very busy right now. Please try again in a minute or type your description manually.")
            return
        }

        val currentModel = modelList[index]
        Log.d("AI_DEBUG", "Connecting to Model: $currentModel")

        val json = JSONObject().apply {
            put("model", currentModel)
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            }))
        }

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://nammahomestay.com")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (index == 0 && e.message?.contains("Unable to resolve host") == true) {
                    handleCallback(callback, "No internet connection detected.")
                } else {
                    tryModel(prompt, index + 1, callback)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                
                if (!response.isSuccessful || body.contains("\"error\"")) {
                    Log.w("AI_RETRY", "$currentModel busy, trying next...")
                    tryModel(prompt, index + 1, callback)
                    return
                }

                try {
                    val text = JSONObject(body).getJSONArray("choices")
                        .getJSONObject(0).getJSONObject("message").getString("content")
                    handleCallback(callback, text)
                } catch (e: Exception) {
                    tryModel(prompt, index + 1, callback)
                }
            }
        })
    }

    private fun handleCallback(callback: (String) -> Unit, message: String) {
        (context as? Activity)?.runOnUiThread { callback(message) }
    }
}
