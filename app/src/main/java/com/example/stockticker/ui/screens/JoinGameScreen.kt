package com.example.stockticker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * @param username       Either real username or "guest"
 * @param token          JWT token (empty if guest)
 * @param onBack         Called when “Back” is tapped
 * @param onJoinSuccess  Called when user successfully joins: passes gameId
 * @param onError        Called when there is an error (e.g. game not found or not in 'waiting')
 */
@Composable
fun JoinGameScreen(
    username: String,
    token: String,
    onBack: () -> Unit,
    onJoinSuccess: (gameId: String) -> Unit,
    onError: (message: String) -> Unit
) {
    var gameId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Join Game by ID", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = gameId,
                onValueChange = { gameId = it },
                label = { Text("Game ID") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    errorMessage = null
                    if (gameId.length != 6) {
                        errorMessage = "Enter a 6‐character ID"
                        return@Button
                    }
                    // TODO: Replace with real socket/VM logic to join
                    // For now, assume any 6‐char ID succeeds:
                    onJoinSuccess(gameId)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinGameScreenPreview() {
    JoinGameScreen(
        username = "guest",
        token = "",
        onBack = {},
        onJoinSuccess = {},
        onError = {}
    )
}
