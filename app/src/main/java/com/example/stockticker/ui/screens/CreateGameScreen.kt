// CreateGameScreen.kt
package com.example.stockticker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Fully functional “Create Game” screen.
 *
 * @param username    The currently‐logged‐in username, or "guest" if playing as guest.
 * @param token       The JWT token (empty string for guest). Passed along to the server.
 * @param onBack      Called when the user taps “Back”.
 * @param onCreate    Called when the user taps “Create”. Parameters:
 *                    (rounds: Int, maxPlayers: Int, aiCount: Int, isPublic: Boolean).
 *                    The NavHost will hook this up to GameViewModel.createGame(...) and handle
 *                    navigation to the LobbyScreen on success.
 */
@Composable
fun CreateGameScreen(
    username: String,
    token: String,
    onBack: () -> Unit,
    onCreate: (rounds: Int, maxPlayers: Int, aiCount: Int, isPublic: Boolean) -> Unit
) {
    var maxPlayers by remember { mutableIntStateOf(2) }
    var rounds by remember { mutableIntStateOf(1) }
    var aiCount by remember { mutableIntStateOf(1) }
    var isPublic by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create New Game",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Max Players Slider
            Text("Players: $maxPlayers")
            Slider(
                value = maxPlayers.toFloat(),
                onValueChange = {
                    val newValue = it.toInt().coerceIn(2, 8)
                    maxPlayers = newValue
                    // Ensure AI count never equals or exceeds maxPlayers
                    if (aiCount >= maxPlayers) {
                        aiCount = maxPlayers - 1
                    }
                },
                valueRange = 2f..8f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AI Opponents Slider
            Text("AI Opponents: $aiCount")
            Slider(
                value = aiCount.toFloat(),
                onValueChange = {
                    aiCount = it.toInt().coerceIn(0, maxPlayers - 1)
                },
                valueRange = 0f..(maxPlayers - 1).toFloat(),
                steps = (maxPlayers - 1).coerceAtLeast(1) - 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rounds Slider
            Text("Rounds: $rounds")
            Slider(
                value = rounds.toFloat(),
                onValueChange = {
                    rounds = it.toInt().coerceIn(1, 100)
                },
                valueRange = 1f..100f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Public Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Make Game Public")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Show error message if any
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Create Game Button
            Button(
                onClick = {
                    // Disable button and clear any previous error
                    isLoading = true
                    errorMessage = null

                    // Invoke parent callback; NavHost will handle navigation on success
                    onCreate(rounds, maxPlayers, aiCount, isPublic)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height( FiftySixDp ) // 56.dp is standard button height
            ) {
                Text(if (isLoading) "Creating..." else "Create Game")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            TextButton(
                onClick = { if (!isLoading) onBack() }
            ) {
                Text("Back")
            }
        }
    }
}

private val FiftySixDp = 56.dp

@Preview(showBackground = true)
@Composable
fun CreateGameScreenPreview() {
    CreateGameScreen(
        username = "Alice",
        token = "dummy-token",
        onBack = { /*no-op*/ },
        onCreate = { rounds, maxPlayers, aiCount, isPublic ->
            /* no-op: Preview only */
        }
    )
}
