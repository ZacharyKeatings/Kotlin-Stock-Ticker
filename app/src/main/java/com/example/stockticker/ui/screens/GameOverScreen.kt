// GameOverScreen.kt
package com.example.stockticker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockticker.viewmodel.GameViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Shows the final leaderboard once the game’s status becomes “complete”.
 *
 * It:
 * 1. Reads `GameViewModel.state.value.game` to get the final game JSON.
 * 2. Computes each player’s total value = cash + (sum of quantity * latest price).
 * 3. Sorts descending, detects ties.
 * 4. Renders a headline (“Winner: …” or “Tie! …”) and a ranked list.
 * 5. When “Return Home” is tapped, emits “game:returnHome” and invokes [onReturnHome].
 *
 * @param gameVm       Shared GameViewModel instance (so we can read final game state and emit “game:returnHome”).
 * @param onReturnHome Callback to navigate back to HomeScreen.
 */
@Composable
fun GameOverScreen(
    gameVm: GameViewModel,
    onReturnHome: () -> Unit
) {
    val uiState by gameVm.state.collectAsState()
    val gameJson = uiState.game

    // If we haven’t yet received the final “game:update” that sets status = "complete", show a spinner.
    if (gameJson == null || gameJson.optString("status") != "complete") {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Waiting for final results…", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    // Now that gameJson.status == "complete", compute each player’s total value:
    val playersArray: JSONArray = gameJson.optJSONArray("players") ?: JSONArray()
    val stocksObject: JSONObject = gameJson.optJSONObject("stocks") ?: JSONObject()

    // Build a list of (username, totalValue) pairs
    data class PlayerResult(val username: String, val totalValue: Double)

    val rankedList: List<PlayerResult> = remember(gameJson) {
        val results = mutableListOf<PlayerResult>()
        for (i in 0 until playersArray.length()) {
            val p = playersArray.getJSONObject(i)
            val username = p.optString("username")
            val cash = p.optDouble("cash", 0.0)

            // Sum portfolioValue = sum over each stock: quantity * current price
            val portfolio = p.optJSONObject("portfolio") ?: JSONObject()
            var portfolioValue = 0.0
            for (key in stocksObject.keys()) {
                val quantity = portfolio.optInt(key, 0)
                val price = stocksObject.optJSONObject(key)?.optDouble("price", 0.0) ?: 0.0
                portfolioValue += (quantity * price)
            }

            val total = (cash + portfolioValue)
            results.add(PlayerResult(username, total))
        }
        // Sort descending by totalValue
        results.sortedByDescending { it.totalValue }
    }

    // Determine top score and whether there's a tie
    val topScore = rankedList.firstOrNull()?.totalValue ?: 0.0
    val topPlayers = rankedList.filter { it.totalValue == topScore }

    // UI:
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Title
            Text(
                text = "🏁 Game Over",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(16.dp))

            // Subtitle: Winner or Tie
            Text(
                text = if (topPlayers.size == 1) {
                    "Winner: ${topPlayers[0].username} with $" +
                            "${"%.2f".format(topPlayers[0].totalValue)}"
                } else {
                    "Tie! Winners: " +
                            topPlayers.joinToString(", ") { it.username } +
                            " at $" + "${"%.2f".format(topScore)}"
                },
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.height(24.dp))

            // Ranked list: #1, #2, #3, …
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // let it scroll if many players
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(rankedList) { index, playerResult ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.width( fortyDp ) // fixed width for alignment
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = playerResult.username,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "$${"%.2f".format(playerResult.totalValue)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Return Home button
            Button(
                onClick = {
                    // Notify server “game:returnHome”
                    gameVm.returnHome(gameJson.optString("id"))
                    onReturnHome()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Return Home")
            }
        }
    }
}

/** A small helper for fixed width; avoids magic numbers in code above. */
private val fortyDp = 40.dp
