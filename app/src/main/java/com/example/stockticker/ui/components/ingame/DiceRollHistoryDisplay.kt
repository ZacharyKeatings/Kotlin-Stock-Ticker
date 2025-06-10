package com.example.stockticker.ui.components.ingame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AutoResizedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    minTextSize: TextUnit = 8.sp,
    maxTextSize: TextUnit = style.fontSize,
    stepGranularity: TextUnit = 1.sp
) {
    var fontSize by remember { mutableStateOf(maxTextSize) }

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontSize = fontSize),
        maxLines = 1,
        softWrap = false,
        onTextLayout = { layoutResult ->
            if (layoutResult.didOverflowWidth && fontSize > minTextSize) {
                // subtract on the raw .value and wrap back to sp
                val newSize = (fontSize.value - stepGranularity.value)
                    .coerceAtLeast(minTextSize.value)
                fontSize = newSize.sp
            }
        }
    )
}

@Composable
fun DiceRollHistoryDisplay(
    historyArray: JSONArray
) {
    // Pull last "Rolled X → Y → Z"
    val last = (0 until historyArray.length())
        .map { historyArray.getJSONObject(it).getString("description") }
        .lastOrNull { it.startsWith("Rolled ") }
        ?: return

    val parts = last.removePrefix("Rolled ").split(" → ").map(String::trim)
    if (parts.size != 3) return

    // Center the row in the middle of the screen
    Box(
        modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            parts.forEach { part ->
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizedText(
                        text = part,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}