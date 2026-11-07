package com.sajada.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorageManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "sajada_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("sajada_insecure_fallback", Context.MODE_PRIVATE)
    }

    fun saveApiKey(key: String) {
        sharedPreferences.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun getApiKey(): String {
        return sharedPreferences.getString("gemini_api_key", "") ?: ""
    }
}
