// StockBoard.kt
package com.example.stockticker.ui.components.ingame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONObject

/**
 * Displays all stocks in a 2×3 grid (two columns, up to three rows).
 */
@Composable
fun StockBoard(
    stocks: JSONObject,
    stockChanges: Map<String, String>,
    priceHistory: Map<String, List<Double>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            "Stock Board",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Gather all stock keys into a List<String>
        val stockKeys = remember(stocks) {
            stocks.keys().asSequence().toList().sorted()
        }

        // Fixed height: 3 rows × 140.dp each, plus 2 gaps of 8.dp between rows
        val gridHeight = ((3 * 100).dp) + ((2 * 8).dp)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                items(stockKeys) { key ->
                    // For each stock symbol, create a StockCard
                    val stockObj = stocks.optJSONObject(key) ?: return@items
                    val price = stockObj.optDouble("price", 0.0)
                    val change = stockChanges[key]
                    val history = priceHistory[key] ?: emptyList()

                    StockCard(
                        name = key,
                        price = price,
                        change = change,
                        history = history
                    )
                }
            }
        )
    }
}

/**
 * A single “card” for one stock. Divided into:
 *  ─ Top half:      [ Stock name (left)  |  Price & Δ (right) ]
 *  ─ Bottom half:   [   Line chart spanning full width   ]
 */
@Composable
fun StockCard(
    name: String,
    price: Double,
    change: String?,
    history: List<Double>
) {
    // Calculate the delta text
    val delta = if (history.size >= 2) {
        val diff = history.last() - history[history.size - 2]
        if (diff >= 0) "+%.2f".format(diff) else "%.2f".format(diff)
    } else {
        "--"
    }

    // Chart color: green if price >= 1.0, else red
    val chartColor = if (price >= 1.0) Color(0xFF22C55E) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), // fixed height to give room for top + bottom sections
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // ─── Top Half: Name (left) and Price/Δ (right) ─────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock name, left‐aligned
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delta on the left
                    Text(
                        text = delta,
                        color = chartColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(4.dp))
                    // Price on the right
                    Text(
                        text = "$${"%.2f".format(price)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ─── Bottom Half: Line Chart spanning full width ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                contentAlignment = Alignment.Center
            ) {
                StockLineChart(
                    data = history,
                    lineColor = chartColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

/**
 * A custom sparkline that:
 * 1. If data is empty, seeds [1.0, 1.0] so the line starts centered.
 * 2. Uses a fixed vertical range 0.0 → 2.0, so 1.0 sits exactly in the middle.
 */
@Composable
fun StockLineChart(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    // 1) Build "actualData" according to your rules:
    //    • If data is empty          ⇒ [1.0, 1.0]
    //    • If data.size == 1         ⇒ [1.0, data[0]]
    //    • Otherwise (size ≥ 2)      ⇒ data
    val actualData: List<Double> = remember(data) {
        when {
            data.isEmpty()    -> listOf(1.0, 1.0)
            data.size == 1    -> listOf(1.0, data[0])
            else              -> data
        }
    }

    Canvas(modifier = modifier) {
        // Vertical range fixed from 0.0 → 2.0 so that 1.0 sits exactly in the middle
        val minValue = 0.0
        val maxValue = 2.0f

        // We know actualData.size >= 2 at this point
        val pointCount = actualData.size
        val stepX = size.width / (pointCount - 1).coerceAtLeast(1)

        // Map each price into an Offset(x, y)
        val points: List<Offset> = actualData.mapIndexed { index, value ->
            val x = index * stepX
            // yRatio = (value - minValue) / (maxValue - minValue)
            val yRatio = ((value - minValue) / (maxValue - minValue)).toFloat().coerceIn(0f, 1f)
            val y = size.height * (1f - yRatio)
            Offset(x, y)
        }

        // Draw line segments between consecutive points
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3f
            )
        }
    }
}
