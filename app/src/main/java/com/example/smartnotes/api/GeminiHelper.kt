package com.example.smartnotes.api

import android.os.Handler
import android.os.Looper
import com.example.smartnotes.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GeminiHelper {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun generateText(prompt: String, callback: (String) -> Unit) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        // Using gemini-1.5-flash-latest as the high-speed "lite" model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent?key=$apiKey"

        // Build JSON safely using Gson
        val jsonRequest = JsonObject().apply {
            val contents = JsonArray().apply {
                add(JsonObject().apply {
                    val parts = JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("text", prompt)
                        })
                    }
                    add("parts", parts)
                })
            }
            add("contents", contents)
        }

        val body = gson.toJson(jsonRequest).toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post { callback("Network Error: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                        val candidates = jsonObject.getAsJsonArray("candidates")
                        if (candidates != null && candidates.size() > 0) {
                            val text = candidates[0].asJsonObject
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")[0].asJsonObject
                                .get("text").asString
                            Handler(Looper.getMainLooper()).post { callback(text) }
                        } else {
                            // This can happen if the model's safety settings block the response.
                            Handler(Looper.getMainLooper()).post { callback("No valid response from AI. The prompt might have been blocked.") }
                        }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post { callback("Parsing Error: ${e.message}") }
                    }
                } else {
                    // Show detailed error for 403 debugging
                    Handler(Looper.getMainLooper()).post { 
                        callback("API Error ${response.code}: Check API Key & Google Cloud settings.") 
                    }
                }
            }
        })
    }
}
