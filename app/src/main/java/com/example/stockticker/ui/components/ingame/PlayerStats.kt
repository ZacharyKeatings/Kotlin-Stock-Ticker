package com.example.stockticker.ui.components.ingame

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.stockticker.ui.theme.Emerald400
import org.json.JSONArray
import org.json.JSONObject

private data class PurchaseLot(val qty: Int, val price: Double)

@Composable
fun PlayerStats(
    players: JSONArray,
    currentTurnPlayerId: String,
    stocks: JSONObject,
    modifier: Modifier = Modifier
) {
    // Convert JSONArray → List<JSONObject>
    val playerList = remember(players) {
        List(players.length()) { players.getJSONObject(it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Player Stats",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(playerList) { playerObj ->
            val playerId   = playerObj.optString("id")
            val username   = playerObj.optString("username")
            val cash       = playerObj.optDouble("cash", 0.0)
            val isCurrent  = playerId == currentTurnPlayerId
            val portfolioJ = playerObj.optJSONObject("portfolio") ?: JSONObject()

            // Build holdings: List of pairs (symbol, lotsList) where lotsList has qty>0
            val holdings: List<Pair<String, List<PurchaseLot>>> = stocks.keys().asSequence()
                .map { symbol ->
                    val lotsJson = portfolioJ.optJSONArray(symbol) ?: JSONArray()
                    val lots = List(lotsJson.length()) { i ->
                        val o = lotsJson.getJSONObject(i)
                        PurchaseLot(o.optInt("qty"), o.optDouble("price"))
                    }
                    symbol to lots
                }
                .filter { (_, lots) -> lots.any { it.qty > 0 } }
                .toList()

            val cardColor = if (isCurrent)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant

            val totalCost = holdings.sumOf {
                (_, lots) -> lots.sumOf {
                    it.qty * it.price
                }
            }
            val totalValue = holdings.sumOf { (symbol, lots) ->
                val currentPrice = stocks.optJSONObject(symbol)?.optDouble("price",0.0) ?: 0.0
                lots.sumOf { it.qty * currentPrice }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCurrent) {
                            BlinkingDot(color = Emerald400)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            username,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Paid: $${"%.2f".format(totalCost)}  \n" +
                                    "Val: $${"%.2f".format(totalValue)}",
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "Cash: $${"%.2f".format(cash)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    if (holdings.isEmpty()) {
                        Text(
                            "No holdings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val rows = remember(holdings) { holdings.chunked(3) }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rows.forEach { rowHoldings ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // build exactly 3 columns per row
                                    for (col in 0 until 3) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            rowHoldings.getOrNull(col)?.let { (symbol, lots) ->
                                                Column {
                                                    Text(
                                                        text = symbol,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                    lots
                                                        .filter { it.qty > 0 }
                                                        .forEach { lot ->
                                                            Text(
                                                                text = "• ${lot.qty} @ $${"%.2f".format(lot.price)}",
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
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
            }
        }
    }
}

@Composable
fun BlinkingDot(
    color: Color = Emerald400,
    size: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val alpha by transition.animateFloat(
        initialValue = 0.9f,
        targetValue  = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        Modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            .background(color, shape = CircleShape)
    )
}
