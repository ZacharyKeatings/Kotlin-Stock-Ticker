package com.example.stockticker.ui.components.ingame

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.TargetConfig
import com.himanshoe.charty.line.MultiLineChart
import com.himanshoe.charty.line.config.InteractionTooltipConfig
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineChartColorConfig
import com.himanshoe.charty.line.model.LineData
import com.himanshoe.charty.line.model.MultiLineData
import org.json.JSONObject

@Composable
fun StockBoard(
    stocks: JSONObject,
    priceHistory: Map<String, List<Double>>,
    modifier: Modifier = Modifier
) {
    val symbols = remember(stocks) { stocks.keys().asSequence().toList().sorted() }
    val palette = listOf(
        Color(0xFF22C55E), // Bonds
        Color(0xFFEF4444), // Gold
        Color(0xFFFACC15), // Grain
        Color(0xFF3B82F6), // Industrial
        Color(0xFF8B5CF6), // Oil
        Color(0xFFF472B6)  // Silver
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement   = Arrangement.spacedBy(12.dp),
        contentPadding        = PaddingValues(0.dp),
        modifier              = modifier.fillMaxWidth()
    ) {
        items(symbols) { symbol ->
            val hist  = priceHistory[symbol].orEmpty()
            val price = stocks
                .optJSONObject(symbol)
                ?.optDouble("price", 0.0)
                ?: 0.0
            val color = palette[symbols.indexOf(symbol) % palette.size]

            StockLineCard(
                symbol  = symbol,
                price   = price,
                history = hist,
                color   = color
            )
        }
    }
}

@Composable
private fun StockLineCard(
    symbol  : String,
    price   : Double,
    history : List<Double>,
    color   : Color
) {
    // 1) Build your real data series (≥2 points, padded)
    val base = when {
        history.isEmpty()  -> listOf(1f, 1f)
        history.size == 1  -> listOf(1f, history[0].toFloat())
        else               -> history.map(Double::toFloat)
    }
    val realFloats = base + base.last()
    val realPoints = realFloats.mapIndexed { idx, y ->
        LineData(xValue = idx.toFloat(), yValue = y)
    }

    // 2) Build invisible anchor series at y=0 and y=2
    val zeroPoints = realFloats.indices.map { i ->
        LineData(xValue = i.toFloat(), yValue = 0f)
    }
    val twoPoints = realFloats.indices.map { i ->
        LineData(xValue = i.toFloat(), yValue = 2f)
    }

    // 3) Color configs
    val baseColorConfig = LineChartColorConfig.default()
    val realColorConfig = baseColorConfig.copy(
        lineColor     = ChartColor.Solid(color),
        lineFillColor = ChartColor.Solid(color.copy(alpha = 0.3f))
    )
    val invisibleConfig = baseColorConfig.copy(
        lineColor     = ChartColor.Solid(Color.Transparent),
        lineFillColor = ChartColor.Solid(Color.Transparent)
    )

    // 4) Pack into MultiLineData
    val multiLineData = listOf(
        MultiLineData(data = realPoints, colorConfig = realColorConfig),
        MultiLineData(data = twoPoints,  colorConfig = invisibleConfig),
        MultiLineData(data = zeroPoints, colorConfig = invisibleConfig)
    )

    val priceColor = if (price >= 1.0) Color(0xFF22C55E) else Color(0xFFEF4444)

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(4.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = symbol,
                    style    = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text  = "$${"%.2f".format(price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = priceColor
                )
            }

            Spacer(Modifier.height(4.dp))

            MultiLineChart(
                data            = { multiLineData },
                modifier        = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                smoothLineCurve = true,
                showFilledArea  = false,
                showLineStroke  = true,
                labelConfig     = LabelConfig.default().copy(
                    showXLabel = false,
                    showYLabel = false
                ),
                chartConfig     = LineChartConfig().copy(
                    interactionTooltipConfig =
                        InteractionTooltipConfig(isLongPressDragEnabled = false)
                ),
                target          = null,
                targetConfig    = TargetConfig.default(),
                onValueChange   = {}
            )
        }
    }
}
