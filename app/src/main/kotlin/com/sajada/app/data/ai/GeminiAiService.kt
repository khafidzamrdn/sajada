package com.sajada.app.data.ai

import com.sajada.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * SECURITY NOTE (Google Cloud Console / Gemini API Restriction):
 * To prevent unauthorized use or extraction of your Gemini API Key in production APKs:
 * 1. Go to Google Cloud Console (APIs & Services > Credentials).
 * 2. Select your Gemini API Key.
 * 3. Under "Application restrictions", choose "Android apps".
 * 4. Add your app's Package Name (com.sajada.app) and your release/debug certificate SHA-1 fingerprint.
 * 5. Under "API restrictions", restrict the key strictly to "Generative Language API".
 * This ensures the key only works inside your signed APK even if decompiled.
 */
class GeminiAiService(private var runtimeApiKey: String = "") {

    fun setApiKey(key: String) {
        runtimeApiKey = key.trim()
    }

    private fun getActiveApiKey(): String {
        if (runtimeApiKey.isNotEmpty()) return runtimeApiKey
        // Fallback to secure BuildConfig injected from local.properties
        val buildConfigKey = BuildConfig.GEMINI_API_KEY
        if (!buildConfigKey.isNullOrBlank()) return buildConfigKey
        return ""
    }

    suspend fun askGemini(prompt: String, appDataContext: String): String = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext "Error: API Key Gemini belum dikonfigurasi. Harap tambahkan GEMINI_API_KEY di local.properties atau melalui menu pengaturan AI."
        }

        try {
            val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val systemPrompt = "Anda adalah asisten AI cerdas untuk Aplikasi Absensi Jamaah 5 Waktu (Sajada). Berikut adalah data real-time aplikasi saat ini:\n$appDataContext\n\nJawablah pertanyaan pengguna dengan ramah, akurat, dan berdasarkan data di atas."
            val fullPrompt = "$systemPrompt\n\nPertanyaan Pengguna: $prompt"

            val escapedPrompt = fullPrompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
            val jsonBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": "$escapedPrompt" }
                      ]
                    }
                  ]
                }
            """.trimIndent()

            connection.outputStream.use { os ->
                os.write(jsonBody.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseStream = BufferedReader(InputStreamReader(connection.inputStream))
                val responseStr = StringBuilder()
                var line = responseStream.readLine()
                while (line != null) {
                    responseStr.append(line)
                    line = responseStream.readLine()
                }
                responseStream.close()

                val raw = responseStr.toString()
                return@withContext parseGeminiResponse(raw)
            } else {
                val errorStream = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream))
                val errStr = StringBuilder()
                var line = errorStream.readLine()
                while (line != null) {
                    errStr.append(line)
                    line = errorStream.readLine()
                }
                return@withContext "Gagal menghubungi Gemini API (HTTP $responseCode): ${errStr.toString().take(200)}"
            }
        } catch (e: Exception) {
            return@withContext "Terjadi kesalahan koneksi: ${e.localizedMessage}"
        }
    }

    private fun parseGeminiResponse(json: String): String {
        try {
            val textMarker = "\"text\": \""
            val startIndex = json.indexOf(textMarker)
            if (startIndex != -1) {
                val start = startIndex + textMarker.length
                var end = start
                var escaped = false
                while (end < json.length) {
                    val char = json[end]
                    if (char == '\\' && !escaped) {
                        escaped = true
                    } else if (char == '"' && !escaped) {
                        break
                    } else {
                        escaped = false
                    }
                    end++
                }
                val extracted = json.substring(start, end)
                return extracted.replace("\\n", "\n").replace("\\\"", "\"")
            }
        } catch (_: Exception) {}
        return "Berhasil menerima respons dari Gemini, namun gagal memparsing JSON."
    }
}
