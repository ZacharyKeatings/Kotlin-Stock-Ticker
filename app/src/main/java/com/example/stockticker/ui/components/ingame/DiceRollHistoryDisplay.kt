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
import androidx.compose.ui.graphics.graphicsLayer

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
    val finalParts = lastRollText.removePrefix("Rolled ").split(" → ").map(String::trim)
    if (finalParts.size != 3) return
    val action = finalParts[1].lowercase()

    // 3) Pools for randomization
    val stockOptions  = listOf("Gold", "Silver", "Oil", "Bonds", "Industrial", "Grain")
    val actionOptions = listOf("up", "down", "dividend")
    val amountOptions = listOf("5", "10", "20")

    // 4) State: what’s currently showing, and per-box shake offsets
    var displayParts by remember { mutableStateOf(finalParts) }
    var offsets      by remember { mutableStateOf(List(3) { 0f to 0f }) }

    // 5) Whenever the roll actually changes, do our “roll animation”
    LaunchedEffect(lastRollText) {
        // shake & randomize for ~6 frames at 75ms each
        repeat(6) {
            displayParts = listOf(
                stockOptions.random(),
                actionOptions.random(),
                amountOptions.random()
            )
            offsets = List(3) {
                // random offset between -6..6 pixels
                ( -6..6 ).random().toFloat() to ( -6..6 ).random().toFloat()
            }
            kotlinx.coroutines.delay(75)
        }
        // settle on the real roll
        displayParts = finalParts
        offsets      = List(3) { 0f to 0f }
    }

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
            displayParts.forEachIndexed { i, part ->
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer {
                            translationX = offsets[i].first
                            translationY = offsets[i].second
                        }
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
                        text     = part,
                        style    = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}