package com.example.stockticker.ui.components.ingame

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private data class ActionEvent(
    val player: String,
    val description: String,
    val round: Int
)

@Composable
fun ActionHistory(
    historyJson: JSONArray?,
    currentRound: Int,
    localUsername: String?
) {
    // 1) Parse into Kotlin list
    val events = remember(historyJson) {
        historyJson
            ?.let { json ->
                List(json.length()) { index ->
                    val obj = json.getJSONObject(index)
                    ActionEvent(
                        player      = obj.optString("player"),
                        description = obj.optString("description"),
                        round       = obj.optInt("round")
                    )
                }
            }
            .orEmpty()
    }

    // 2) Group by round, newest rounds first, and reverse each subgroup
    val groupedByRound = remember(events) {
        events
            .groupBy { it.round }
            .map { (round, evList) -> round to evList.asReversed() }
            .sortedByDescending { it.first }
    }

    // 3) Track which rounds are expanded
    var expandedRounds by remember { mutableStateOf(setOf(currentRound)) }
    fun toggleRound(round: Int) {
        if (round == currentRound) return
        expandedRounds = if (expandedRounds.contains(round)) {
            expandedRounds - round
        } else {
            expandedRounds + round
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("📋 Action History", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
        }

        if (groupedByRound.isEmpty()) {
            item {
                Text(
                    "No actions yet...",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                )
            }
        } else {
            groupedByRound.forEach { (round, eventsInRound) ->
                // Header
                item {
                    val isExpanded = expandedRounds.contains(round)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { toggleRound(round) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (round == currentRound) "Current Round" else "Round $round",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                if (expandedRounds.contains(round)) {
                    items(eventsInRound) { event ->
                        val age = currentRound - event.round
                        val ageLabel = if (age <= 0) "Just now" else "$age round${if (age > 1) "s" else ""} ago"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        event.player,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        ageLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    event.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
