package com.example.stockticker.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject
import java.util.UUID

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val TOKEN_KEY = "token"
    private const val GUEST_KEY = "guest-identity"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply()
    }

    fun getToken(): String? = prefs.getString(TOKEN_KEY, null)

    fun getCurrentUser(): UserIdentity {
        getToken()?.let { token ->
            try {
                val parts = token.split(".")
                if (parts.size == 3) {
                    val decoded = String(Base64.decode(parts[1], Base64.URL_SAFE))
                    val json = JSONObject(decoded)
                    val username = json.getString("username")
                    return UserIdentity("registered", username, token)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to guest
        val guest = prefs.getString(GUEST_KEY, null)
            ?: generateGuestUsername().also {
                prefs.edit().putString(GUEST_KEY, it).apply()
            }

        return UserIdentity("guest", guest, null)
    }

    private fun generateGuestUsername(): String {
        val suffix = UUID.randomUUID().toString().take(4)
        return "Guest$suffix"
    }
}

data class UserIdentity(
    val type: String, // "registered" or "guest"
    val username: String,
    val token: String? // null for guest
)
