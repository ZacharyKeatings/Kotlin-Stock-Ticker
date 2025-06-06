package com.example.stockticker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stockticker.auth.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * A fully‐functional registration screen. On success, calls [onRegisterSuccess] with (username, token),
 * and also stores the token in AuthManager so future app launches skip to HomeScreen.
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: (username: String, token: String) -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = {
                    errorMessage = null
                    when {
                        username.isBlank() || email.isBlank() || password.isBlank() -> {
                            errorMessage = "All fields are required"
                            return@Button
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    val (regUsername, token) =
                                        performRegisterRequest(username.trim(), email.trim(), password)

                                    // Save the token in AuthManager so next app launch is auto‐logged‐in
                                    AuthManager.saveToken(token)

                                    onRegisterSuccess(regUsername, token)
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Registration failed"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (isLoading) "Registering..." else "Register")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

/**
 * Performs registration by POSTing to /api/auth/register, then fetching /api/auth/profile.
 * Returns Pair(username, token) on success.
 */
private suspend fun performRegisterRequest(
    username: String,
    email: String,
    password: String
): Pair<String, String> {
    return withContext(Dispatchers.IO) {
        val registerJson = JSONObject().apply {
            put("username", username)
            put("email", email)
            put("password", password)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = registerJson.toString().toRequestBody(mediaType)
        val client = OkHttpClient()

        val registerRequest = Request.Builder()
            .url("https://ticker.cinefiles.dev/api/auth/register")
            .post(requestBody)
            .build()

        val registerResponse = try {
            client.newCall(registerRequest).execute()
        } catch (e: IOException) {
            throw Exception("Network error: ${e.localizedMessage}")
        }

        if (!registerResponse.isSuccessful) {
            val errorBody = registerResponse.body?.string().orEmpty()
            val message = try {
                JSONObject(errorBody).optString("message", registerResponse.message)
            } catch (_: Exception) {
                registerResponse.message
            }
            throw Exception("Registration failed: $message")
        }

        val registerBodyString = registerResponse.body?.string().orEmpty()
        val registerObj = try {
            JSONObject(registerBodyString)
        } catch (e: Exception) {
            throw Exception("Invalid server response during registration")
        }
        val token = registerObj.optString("token")
        if (token.isBlank()) {
            throw Exception("Registration failed: no token returned")
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
        val returnedUsername = profileObj.optString("username")
        if (returnedUsername.isBlank()) {
            throw Exception("Profile missing username")
        }

        Pair(returnedUsername, token)
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onRegisterSuccess = { _, _ -> }, onBack = {})
}
