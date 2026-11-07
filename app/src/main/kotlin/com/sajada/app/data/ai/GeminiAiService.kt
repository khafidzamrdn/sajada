package com.sajada.app.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GeminiAiService(private var apiKey: String = "") {

    fun setApiKey(key: String) {
        apiKey = key.trim()
    }

    suspend fun askGemini(prompt: String, appDataContext: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) {
            return@withContext "Error: API Key Gemini belum diatur. Silakan masukkan API Key Anda melalui tombol pengaturan AI."
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

            // Construct JSON payload safely without external gson library
            val escapedPrompt = fullPrompt.replace("\"", "\\\"").replace("\n", "\\n")
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
                var line: String?
                while (responseStream.readLine().also { line = line } != null) {
                    responseStr.append(line)
                }
                responseStream.close()

                // Simple JSON extraction for text response
                val raw = responseStr.toString()
                return@withContext parseGeminiResponse(raw)
            } else {
                val errorStream = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream))
                val errStr = StringBuilder()
                var line: String?
                while (errorStream.readLine().also { line = line } != null) {
                    errStr.append(line)
                }
                return@withContext "Gagal menghubungi Gemini API (HTTP $responseCode): ${errStr.toString().take(200)}"
            }
        } catch (e: Exception) {
            return@withContext "Terjadi kesalahan koneksi: ${e.localizedMessage}"
        }
    }

    private fun parseGeminiResponse(json: String): String {
        try {
            // Extract text field from standard Gemini JSON response structure using string indexing / regex
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
