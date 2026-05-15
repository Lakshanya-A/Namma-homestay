package com.example.nammahomestay.ui.host

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class GeminiService(private val context: Context) {

    // 🔑 REPLACE WITH YOUR NEW GEMINI API KEY
    private val apiKey = "AIzaSyBYRxEnmg-2XpnZx1a8uEmMlHJ4d0aNltk"

    private val client = OkHttpClient()

    fun generateDescription(
        prompt: String,
        callback: (String) -> Unit
    ) {

        // ✅ CREATE JSON REQUEST

        val part = JSONObject()
        part.put("text", prompt)

        val partsArray = JSONArray()
        partsArray.put(part)

        val content = JSONObject()
        content.put("parts", partsArray)

        val contentsArray = JSONArray()
        contentsArray.put(content)

        val json = JSONObject()
        json.put("contents", contentsArray)

        // ✅ REQUEST BODY

        val body = json.toString()
            .toRequestBody(
                "application/json".toMediaType()
            )

        // ✅ API REQUEST

        val request = Request.Builder()
            .url(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"            )
            .post(body)
            .build()

        // ✅ API CALL

        client.newCall(request)
            .enqueue(object : Callback {

                // ❌ FAILURE
                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {

                    Log.e(
                        "GEMINI_ERROR",
                        e.message.toString()
                    )

                    (context as Activity).runOnUiThread {

                        Toast.makeText(
                            context,
                            "Internet or API failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // ✅ RESPONSE
                override fun onResponse(
                    call: Call,
                    response: Response
                ) {

                    val responseText =
                        response.body?.string()

                    Log.d(
                        "GEMINI_RESPONSE",
                        responseText ?: "EMPTY"
                    )

                    try {

                        // ✅ EMPTY RESPONSE CHECK

                        if (responseText.isNullOrEmpty()) {

                            (context as Activity).runOnUiThread {

                                Toast.makeText(
                                    context,
                                    "Empty AI response",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            return
                        }

                        val jsonResponse =
                            JSONObject(responseText)

                        // ✅ HANDLE API ERRORS

                        if (jsonResponse.has("error")) {

                            val errorMessage =
                                jsonResponse
                                    .getJSONObject("error")
                                    .getString("message")

                            (context as Activity).runOnUiThread {

                                Toast.makeText(
                                    context,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        // ✅ SAFE RESPONSE CHECK

                        val candidates =
                            jsonResponse.optJSONArray("candidates")

                        if (candidates == null ||
                            candidates.length() == 0
                        ) {

                            (context as Activity).runOnUiThread {

                                Toast.makeText(
                                    context,
                                    "No AI response",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        // ✅ EXTRACT TEXT

                        val result = candidates
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        // ✅ RETURN RESULT

                        (context as Activity).runOnUiThread {

                            callback(result)
                        }

                    } catch (e: Exception) {

                        Log.e(
                            "PARSE_ERROR",
                            e.message.toString()
                        )

                        (context as Activity).runOnUiThread {

                            Toast.makeText(
                                context,
                                "Parse failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            })
    }
}