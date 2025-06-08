// LobbyScreen.kt
package com.example.stockticker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stockticker.ui.theme.*
import com.example.stockticker.viewmodel.GameViewModel
import com.example.stockticker.viewmodel.LobbyViewModel
import org.json.JSONArray
import org.json.JSONObject

/**
 * Fully‐functional Lobby Screen.
 *
 * Shows the list of joined players, lets the host start the countdown,
 * and automatically navigates into the InGameScreen once the server transitions
 * to “initial-buy” (i.e. game status changes).
 *
 * @param navController   Used to navigate to InGameScreen or back home.
 * @param gameId          The 6‐character game ID.
 * @param username        The current user’s username (or null if guest).
 * @param token           The JWT token (or null if guest).
 */
@Composable
fun LobbyScreen(
    navController: NavController,
    gameId: String,
    username: String?,
    token: String?,
    gameVm: GameViewModel
) {
    val uiState by gameVm.state.collectAsState()
    val gameJson = uiState.game
    val status = gameJson?.optString("status") ?: "waiting"
    val maxPlayers = gameJson?.optInt("maxPlayers") ?: 0

    val lobbyVm: LobbyViewModel = viewModel()
    val lobbyState by lobbyVm.state.collectAsState()
    val countdownSeconds = lobbyState.countdown

    val playerList = remember(gameJson) {
        val arr = gameJson?.optJSONArray("players")
        List(arr?.length() ?: 0) { idx ->
            arr?.optJSONObject(idx)?.optString("username") ?: "Unknown"
        }
    }

    val hostUsername = remember(gameJson) {
        gameJson?.optJSONArray("players")?.optJSONObject(0)?.optString("username")
    }

    LaunchedEffect(status) {
        if (status == "initial-buy") {
            navController.navigate("inGame/$gameId") {
                popUpTo("lobby/$gameId") { inclusive = true }
            }
        }
    }

    val cardColor = Slate800.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate900, Slate600)))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BorderStroke(1.dp, Slate600),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Game Lobby",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall)

                    Text(
                        "Game ID: $gameId",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Waiting for players (${playerList.size}/$maxPlayers)",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(playerList) { name ->
                            Text("• $name", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    if (countdownSeconds != null) {
                        Text("Game starting in $countdownSeconds...", style = MaterialTheme.typography.bodyLarge)
                    } else if (status == "waiting" && username != null && username == hostUsername) {
                        StyledButton("Start Game") {
                            gameVm.startGame(gameId)
                        }
                    }

                    if (status == "waiting") {
                        TextButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Home", color = Emerald500)
                        }
                    }
                }
            }
        }
    }
}

