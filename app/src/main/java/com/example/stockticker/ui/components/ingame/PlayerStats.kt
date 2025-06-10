package com.example.stockticker.ui.components.ingame

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.stockticker.ui.theme.Emerald400
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun PlayerStats(
    players: JSONArray,
    currentTurnPlayerId: String,
    stocks: JSONObject,
    modifier: Modifier = Modifier
) {
    // Turn the JSONArray into a Kotlin List<JSONObject> for easy iteration
    val playerList = remember(players) {
        List(players.length()) { idx -> players.getJSONObject(idx) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item {
            Text(
                text = "Player Stats",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // One item per player
        items(playerList) { playerObj ->
            val playerId  = playerObj.optString("id")
            val username  = playerObj.optString("username")
            val cash      = playerObj.optDouble("cash", 0.0)
            val isCurrent = playerId == currentTurnPlayerId
            val portfolio = playerObj.optJSONObject("portfolio") ?: JSONObject()

            // Build a list of (symbol, qty) where qty > 0
            val nonZeroHoldings = stocks.keys().asSequence()
                .map { symbol -> symbol to (portfolio.optInt(symbol, 0)) }
                .filter { it.second > 0 }
                .toList()

            // Card background varies if it's the current player
            val cardColor = if (isCurrent)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant

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
                            text = username,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Cash: $${"%.2f".format(cash)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    if (nonZeroHoldings.isEmpty()) {
                        Text(
                            text = "No holdings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Show up to two holdings per row
                            nonZeroHoldings.chunked(2).forEach { rowList ->
                                Column(modifier = Modifier.weight(1f)) {
                                    rowList.forEach { (symbol, qty) ->
                                        Text(
                                            text = "$symbol: $qty",
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


@Composable
fun BlinkingDot(
    color: Color = Emerald400,
    size: Dp = 8.dp // matches w-2 h-2 in Tailwind (~8px)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "scaleAnim"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "alphaAnim"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(color = color, shape = CircleShape)
    )
}