package com.example.stockticker.ui.components.ingame

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

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
    // 1) Extract the last “Rolled X → Y → Z” string
    val lastRollText: String? = (0 until historyArray.length())
        .map { historyArray.getJSONObject(it).getString("description") }
        .lastOrNull { it.startsWith("Rolled ") }

    if (lastRollText == null) return

    // 2) Split into the three parts
    val parts = lastRollText.removePrefix("Rolled ").split(" → ").map(String::trim)
    if (parts.size != 3) return
    val action = parts[1].lowercase()

    // 3) Flash state: toggles true on each new roll
    var flash by remember { mutableStateOf(false) }
    LaunchedEffect(lastRollText) {
        flash = true
        // stay green for 600ms
        kotlinx.coroutines.delay(600)
        flash = false
    }
    val flashColor = when (action) {
        "up"       -> Color(0xFF22C55E)  // green
        "down"     -> Color(0xFFEF4444)  // red
        "dividend" -> Color(0xFFFACC15)  // yellow
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // 4) Animate border color between transparent and green
    val borderColor by animateColorAsState(
        targetValue = if (flash) flashColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 500, easing = FastOutLinearInEasing)
    )

    val borderWidth by animateDpAsState(
        targetValue = if (flash) 3.dp else 1.dp,
        animationSpec = tween(500, easing = FastOutLinearInEasing)
    )

    // 5) Draw the row with that border
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .padding(vertical = 4.dp), // inner padding so the border isn’t tight
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment   = Alignment.CenterVertically
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
                            width = borderWidth,
                            color = borderColor,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizedText(
                        text     = part,
                        style    = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}