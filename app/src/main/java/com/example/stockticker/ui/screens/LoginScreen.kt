// LoginScreen.kt
package com.example.stockticker.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockticker.auth.AuthManager
import com.example.stockticker.auth.UserIdentity
import com.example.stockticker.network.SocketManager
import com.example.stockticker.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * A fully‐functional login screen. On success, calls [onLoginSuccess] with (username, token),
 * but also stores the token in AuthManager so future app launches will skip straight to HomeScreen.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (username: String, token: String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate900, Slate800)))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BorderStroke(1.dp, Slate600)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Login", style = MaterialTheme.typography.headlineSmall)

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    StyledButton(
                        text = if (isLoading) "Logging in..." else "Login",
                        onClick = {
                            errorMessage = null
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Email and password must not be empty"
                                return@StyledButton
                            }
                            isLoading = true
                            scope.launch {
                                try {
                                    val (username, token) = performLoginRequest(email.trim(), password)
                                    AuthManager.saveToken(token)
                                    onLoginSuccess(username, token)
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Login failed"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    TextButton(onClick = onBack) {
                        Text("Back", color = Emerald500)
                    }
                }
            }
        }
    }
}


/**
 * Looks up /api/auth/login and /api/auth/profile exactly as before.
 * Returns Pair(username, token) on success.
 */
private suspend fun performLoginRequest(email: String, password: String): Pair<String, String> {
    return withContext(Dispatchers.IO) {
        val loginJson = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = loginJson.toString().toRequestBody(mediaType)
        val client = OkHttpClient()

        val loginRequest = Request.Builder()
            .url("https://ticker.cinefiles.dev/api/auth/login")
            .post(requestBody)
            .build()

        val loginResponse = try {
            client.newCall(loginRequest).execute()
        } catch (e: IOException) {
            throw Exception("Network error: ${e.localizedMessage}")
        }

        if (!loginResponse.isSuccessful) {
            val errorBody = loginResponse.body?.string().orEmpty()
            val message = try {
                JSONObject(errorBody).optString("message", loginResponse.message)
            } catch (_: Exception) {
                loginResponse.message
            }
            throw Exception("Login failed: $message")
        }

        val loginBodyString = loginResponse.body?.string().orEmpty()
        val loginObj = try {
            JSONObject(loginBodyString)
        } catch (e: Exception) {
            throw Exception("Invalid server response")
        }
        val token = loginObj.optString("token")
        if (token.isBlank()) {
            throw Exception("Login failed: no token returned")
        }

        // Fetch profile to confirm username
        val profileRequest = Request.Builder()
            .url("https://ticker.cinefiles.dev/api/auth/profile")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        val profileResponse = try {
            client.newCall(profileRequest).execute()
        } catch (e: IOException) {
            throw Exception("Network error: ${e.localizedMessage}")
        }

        if (!profileResponse.isSuccessful) {
            val errorBody = profileResponse.body?.string().orEmpty()
            val message = try {
                JSONObject(errorBody).optString("message", profileResponse.message)
            } catch (_: Exception) {
                profileResponse.message
            }
            throw Exception("Fetch profile failed: $message")
        }

        val profileBodyString = profileResponse.body?.string().orEmpty()
        val profileObj = try {
            JSONObject(profileBodyString)
        } catch (e: Exception) {
            throw Exception("Invalid profile response")
        }
        val username = profileObj.optString("username")
        if (username.isBlank()) {
            throw Exception("Profile missing username")
        }

        Pair(username, token)
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = { _, _ -> }, onBack = {})
}
