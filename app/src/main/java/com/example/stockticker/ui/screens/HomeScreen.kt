package com.example.stockticker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * If `username == "guest"`, then this is the guest variant (token is ignored).
 * Otherwise a logged‐in user.
 *
 * @param username      Either a real username or the literal string "guest".
 * @param token         JWT token (empty string for "guest").
 * @param onCreateGame  Called when the user taps “Create Game”.
 * @param onJoinGame    Called when the user taps “Join Game by ID”.
 * @param onFindGame    Called when the user taps “Find Public Game”.
 * @param onRejoinGame  Called when the user taps “Rejoin My Active Game”. Pass a gameId.
 * @param onProfile     Called when the user taps the “Profile” icon. Null if guest.
 * @param onSettings    Called when the user taps the “Settings” icon. Null if guest.
 */
@Composable
fun HomeScreen(
    username: String,
    token: String,
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    onFindGame: () -> Unit,
    onRejoinGame: ((gameId: String) -> Unit)?,
    onProfile: (() -> Unit)?,
    onSettings: (() -> Unit)?
) {
    // TODO: Replace with your real UI. For now, just show buttons in a Column.
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Home Screen", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("User: $username", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onCreateGame, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Create Game")
            }
            Button(onClick = onJoinGame, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Join Game by ID")
            }
            Button(onClick = onFindGame, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Find Public Games")
            }

            if (onRejoinGame != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onRejoinGame("SOME_GAME_ID") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Rejoin My Game")
                }
            }

            if (onProfile != null || onSettings != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    if (onProfile != null) {
                        Button(onClick = { onProfile() }, modifier = Modifier.weight(1f)) {
                            Text("Profile")
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    if (onSettings != null) {
                        Button(onClick = { onSettings() }, modifier = Modifier.weight(1f)) {
                            Text("Settings")
                        }
                    }
                }
            }
        }
    }
}
