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
import org.json.JSONArray
import org.json.JSONObject

/**
 * Shows the final leaderboard once the game’s status becomes “complete”.
 *
 * Reads `GameViewModel.state.value.game`, sums each player’s:
 *  • cash
 *  • sum over all lots in their portfolio: qty * current market price
 * Then sorts descending and displays the ranking.
 */
@Composable
fun GameOverScreen(
    gameVm: GameViewModel,
    onReturnHome: () -> Unit
) {
    val uiState by gameVm.state.collectAsState()
    val gameJson = uiState.game

    // While waiting for final state:
    if (gameJson == null || gameJson.optString("status") != "complete") {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Waiting for final results…",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    // Extract arrays/objects
    val playersArray = gameJson.optJSONArray("players") ?: JSONArray()
    val stocksObject = gameJson.optJSONObject("stocks") ?: JSONObject()

    // Compute ranking once
    data class PlayerResult(val username: String, val totalValue: Double)
    val rankedList: List<PlayerResult> = remember(gameJson) {
        val results = mutableListOf<PlayerResult>()
        for (i in 0 until playersArray.length()) {
            val p = playersArray.getJSONObject(i)
            val username = p.optString("username")
            val cash     = p.optDouble("cash", 0.0)

            // Sum portfolioValue = sum over each stock's lots
            val portfolioJson = p.optJSONObject("portfolio") ?: JSONObject()
            var portfolioValue = 0.0
            stocksObject.keys().forEach { stockKey ->
                val price = stocksObject
                    .optJSONObject(stockKey)
                    ?.optDouble("price", 0.0) ?: 0.0
                val lotsArray = portfolioJson.optJSONArray(stockKey)
                if (lotsArray != null) {
                    for (j in 0 until lotsArray.length()) {
                        val lot = lotsArray.getJSONObject(j)
                        val qty = lot.optInt("qty", 0)
                        portfolioValue += qty * price
                    }
                }
            }

            val total = cash + portfolioValue
            results.add(PlayerResult(username, total))
        }
        results.sortedByDescending { it.totalValue }
    }

    // Determine winners / ties
    val topScore  = rankedList.firstOrNull()?.totalValue ?: 0.0
    val topPlayers = rankedList.filter { it.totalValue == topScore }

    // UI
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                "🏁 Game Over",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = if (topPlayers.size == 1) {
                    "Winner: ${topPlayers[0].username} with $" +
                            "${"%.2f".format(topPlayers[0].totalValue)}"
                } else {
                    "Tie! Winners: " + topPlayers.joinToString(", ") { it.username } +
                            " at $" + "${"%.2f".format(topScore)}"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(rankedList) { index, playerResult ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
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
                                "#${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.width(rankLabelWidth)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                playerResult.username,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$${"%.2f".format(playerResult.totalValue)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
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

// fixed width for the ranking label column:
private val rankLabelWidth = 40.dp
