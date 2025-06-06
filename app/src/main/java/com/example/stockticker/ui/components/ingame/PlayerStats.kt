package com.example.stockticker.ui.components.ingame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

/**
 * A PlayerStats panel that shows each player’s name, cash, and a 2-column grid of their non-zero holdings.
 */
@Composable
fun PlayerStats(
    players: JSONArray,
    currentTurnPlayerId: String,
    stocks: JSONObject
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Player Stats",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(8.dp))

        // For each player...
        for (i in 0 until players.length()) {
            val playerObj = players.getJSONObject(i)
            val playerId = playerObj.optString("id")
            val username = playerObj.optString("username")
            val cash = playerObj.optDouble("cash", 0.0)
            val isCurrent = (playerId == currentTurnPlayerId)
            val portfolio = playerObj.optJSONObject("portfolio") ?: JSONObject()

            // Collect only those holdings where quantity > 0
            val nonZeroHoldings: List<Pair<String, Int>> = stocks.keys().asSequence()
                .map { symbol -> symbol to (portfolio.optInt(symbol, 0)) }
                .filter { it.second > 0 }
                .toList()

            // Split holdings into rows of 2
            val rowsOfHoldings = remember(nonZeroHoldings) {
                nonZeroHoldings.chunked(2)
            }

            // Choose a slightly different container color if this is the current player
            val cardColor = if (isCurrent) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // ─── Top row: “▶ Username” (if current) and Cash (right-aligned) ───────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCurrent) "▶ $username" else username,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Cash: $${"%.2f".format(cash)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // ─── If there are no holdings, say so ──────────────────────────────────────
                    if (nonZeroHoldings.isEmpty()) {
                        Text(
                            text = "No holdings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Holdings:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))

                        // ─── Two-column grid of holdings ─────────────────────────────────────────
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (row in rowsOfHoldings) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // First cell in this row
                                    val (symbol0, qty0) = row[0]
                                    Text(
                                        text = "$symbol0: $qty0",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Second cell (if exists)
                                    if (row.size > 1) {
                                        val (symbol1, qty1) = row[1]
                                        Text(
                                            text = "$symbol1: $qty1",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        // empty placeholder so weights line up
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}