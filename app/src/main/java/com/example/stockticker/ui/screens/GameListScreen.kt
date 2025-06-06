// GameListScreen.kt
package com.example.stockticker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @param username      Either real username or "guest"
 * @param token         JWT token (empty if guest)
 * @param onJoin        Called when the user taps “Join” next to a specific gameId
 * @param onBack        Called when the user taps “Back”
 */
@Composable
fun GameListScreen(
    username: String,
    token: String,
    onJoin: (gameId: String) -> Unit,
    onBack: () -> Unit
) {
    // TODO: Replace with live data from server. For now, show a static list.
    val dummyGames = listOf("ABC123", "DEF456", "GHI789")

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Public Games", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(dummyGames) { gameId ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Game ID: $gameId", style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = { onJoin(gameId) }) {
                            Text("Join")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}
