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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.LineChart
import org.json.JSONObject
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.ChartViewDefaults
import io.github.dautovicharis.charts.style.LineChartDefaults

/**
 * Displays all stocks in a 2×3 grid (two columns, up to three rows).
 */
@Composable
fun StockBoard(
    stocks: JSONObject,
    stockChanges: Map<String, String>,
    priceHistory: Map<String, List<Double>>,
    modifier: Modifier = Modifier
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
        val gridHeight = ((3 * 210).dp) + ((2 * 8).dp)

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
            .height(290.dp), // fixed height to give room for top + bottom sections
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            // ─── Top Half: Name (left) and Price/Δ (right) ─────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock name, left‐aligned
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).padding(4.dp)
                )

                Row(
                    modifier = Modifier.weight(1f).padding(4.dp),
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
                        .padding(horizontal = 0.dp)
                )
            }
//            Text(
//                text = "History: " + history.joinToString(
//                    prefix = "[", postfix = "]"
//                ) { value -> "%.2f".format(value) },
//                style    = MaterialTheme.typography.labelSmall,
//                color    = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(4.dp)
//            )
        }
    }
}

@Composable
fun StockLineChart(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    // 1) Normalize and convert to Float
    val baseSeries: List<Float> = remember(data) {
        when {
            data.isEmpty()  -> listOf(1.0f, 1.0f)
            data.size == 1  -> listOf(1.0f, data[0].toFloat())
            else            -> data.map(Double::toFloat)
        }
    }

    val floatData: List<Float> = remember(baseSeries) {
        baseSeries + listOf(baseSeries.last())
    }

    // 2) Build the ChartDataSet
    val dataSet = floatData.toChartDataSet(
        title   = "",
        postfix = ""
    )

    // 3) Customize style:
    val style = LineChartDefaults.style(
        lineColor         = lineColor.toArgb().let { Color(it) },
        // remove all point circles:
        pointSize         = 0f,
        pointVisible      = false,
        bezier            = false
    )

    // 4) Wrap in Box so you can size it exactly:
    Box(
        modifier = modifier.fillMaxWidth()
            // enforce a minimum width & height in dp:
//            .sizeIn(minWidth = 150.dp, minHeight = 80.dp)
    ) {
        LineChart(
            dataSet = dataSet,
            style   = style
        )
    }
}