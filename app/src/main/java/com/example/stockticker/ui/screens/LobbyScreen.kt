// LobbyScreen.kt
package com.example.stockticker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

    // 2) Collect the UI state from the ViewModel
    val uiState by gameVm.state.collectAsState()

    // 3) Extract the game JSON object from the state
    val gameJson: JSONObject? = uiState.game

    // 4) Extract countdown (if any)
    val lobbyVm: LobbyViewModel = viewModel()
    val lobbyState by lobbyVm.state.collectAsState()
    val countdownSeconds: Int? = lobbyState.countdown

    // 5) Extract status field (waiting / initial-buy / active / complete)
    val status: String = gameJson?.optString("status") ?: "waiting"

    // 6) When status changes to "initial-buy", transition to the InGameScreen
    LaunchedEffect(status) {
        if (status == "initial-buy") {
            // Construct the correct route arguments: if guest, username will be "guest" and token = ""
            val actualUsername = username ?: "guest"
            val actualToken = token ?: ""
            navController.navigate("inGame/$gameId") {
                // Pop the lobby off the back stack so the user can't go back to it
                popUpTo("lobby/$gameId") { inclusive = true }
            }
        }
    }

    // 7) Build a list of current player usernames
    val playerList: List<String> = remember(gameJson) {
        val arr: JSONArray? = gameJson?.optJSONArray("players")
        if (arr == null) {
            emptyList()
        } else {
            List(arr.length()) { idx ->
                arr.optJSONObject(idx)?.optString("username") ?: "Unknown"
            }
        }
    }

    // 8) Determine host: by convention, the first player in the array is the host
    val hostUsername: String? = remember(gameJson) {
        gameJson?.optJSONArray("players")
            ?.optJSONObject(0)
            ?.optString("username")
    }

    // 9) UI
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title and Game ID
            Text(
                text = "Lobby: $gameId",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()

            // Player list header
            Text(
                text = "Players Joined (${playerList.size}/${gameJson?.optInt("maxPlayers") ?: "-"}):",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // LazyColumn for player names
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(playerList) { playerName ->
                    Text(
                        text = "• $playerName",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // If we have a countdown running, show it. Otherwise, if we're the host and still waiting,
            // show the "Start Game" button.
            if (countdownSeconds != null) {
                Text(
                    text = "Game starting in $countdownSeconds...",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // Only show the Start Game button if:
                //  • Status is still "waiting"
                //  • The current user is the host (hostUsername == username)
                if (status == "waiting" && username != null && username == hostUsername) {
                    Button(
                        onClick = {
                            // Call into VM to emit "game:start"
                            gameVm.startGame(gameId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Game")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Always show a Back button to return home if the game hasn't started yet
            if (status == "waiting") {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        // Simply pop back to the HomeScreen
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}
