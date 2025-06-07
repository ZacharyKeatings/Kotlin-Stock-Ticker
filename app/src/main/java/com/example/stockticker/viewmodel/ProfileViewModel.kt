package com.example.stockticker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockticker.auth.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class ProfileUiState(
    val username: String?       = null,
    val email: String?          = null,
    val joined: String?         = null,
    val gamesPlayed: Int?       = null,
    val gamesWon: Int?          = null,
    val gamesLost: Int?         = null,
    val totalStocksBought: Int? = null,
    val totalStocksSold: Int?   = null,
    val totalMoneyEarned: Double? = null,
    val totalMoneySpent: Double?  = null,
    val error: String?          = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val client = OkHttpClient()
    private val baseUrl = "https://ticker.cinefiles.dev"

    init {
        fetchProfileAndStats()
    }

    private fun fetchProfileAndStats() {
        val user  = AuthManager.getCurrentUser()
        val token = user.token ?: run {
            _uiState.value = ProfileUiState(error = "Not authenticated")
            return
        }

        viewModelScope.launch {
            try {
                // ── 1) PROFILE ─────────────────────────────────────────────
                val profileReq = Request.Builder()
                    .url("$baseUrl/api/auth/profile")
                    .header("Authorization", "Bearer $token")
                    .build()

                val profileResp = withContext(Dispatchers.IO) {
                    client.newCall(profileReq).execute()
                }
                if (!profileResp.isSuccessful) {
                    throw Exception("Profile request failed: ${profileResp.code}")
                }
                val pj = profileResp.body!!.string().let { JSONObject(it) }
                val username = pj.getString("username")
                val email    = pj.getString("email")
                val rawJoined= pj.getString("createdAt")
                val joined   = try {
                    // assume ISO timestamp like "2025-06-01T12:34:56Z"
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    parser.timeZone = TimeZone.getTimeZone("UTC")
                    val date = parser.parse(rawJoined) ?: Date()
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
                } catch (_: Exception) {
                    rawJoined
                }

                // ── 2) STATS ───────────────────────────────────────────────
                val statsReq = Request.Builder()
                    .url("$baseUrl/api/auth/stats")
                    .header("Authorization", "Bearer $token")
                    .build()

                val statsResp = withContext(Dispatchers.IO) {
                    client.newCall(statsReq).execute()
                }
                if (!statsResp.isSuccessful) {
                    throw Exception("Stats request failed: ${statsResp.code}")
                }
                val sj = statsResp.body!!.string().let { JSONObject(it) }

                // pull out whichever fields you need:
                val gamesPlayed      = sj.optInt("gamesPlayed")
                val gamesWon         = sj.optInt("gamesWon")
                val gamesLost        = sj.optInt("gamesLost")
                val totalStocksBought= sj.optInt("totalStocksBought")
                val totalStocksSold  = sj.optInt("totalStocksSold")
                val totalMoneyEarned = sj.optDouble("totalMoneyEarned")
                val totalMoneySpent  = sj.optDouble("totalMoneySpent")

                // ── UPDATE UI STATE ────────────────────────────────────────
                _uiState.value = ProfileUiState(
                    username            = username,
                    email               = email,
                    joined              = joined,
                    gamesPlayed         = gamesPlayed,
                    gamesWon            = gamesWon,
                    gamesLost           = gamesLost,
                    totalStocksBought   = totalStocksBought,
                    totalStocksSold     = totalStocksSold,
                    totalMoneyEarned    = totalMoneyEarned,
                    totalMoneySpent     = totalMoneySpent
                )
            } catch (t: Throwable) {
                _uiState.value = ProfileUiState(error = t.localizedMessage ?: "Unknown error")
            }
        }
    }
}
