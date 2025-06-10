package com.example.stockticker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stockticker.ui.theme.*

@Composable
fun CreateGameScreen(
    username: String,
    token: String,
    onBack: () -> Unit,
    onCreate: (rounds: Int, maxPlayers: Int, aiCount: Int, isPublic: Boolean) -> Unit
) {
    var maxPlayers by remember { mutableIntStateOf(4) }
    var rounds by remember { mutableIntStateOf(10) }
    var aiCount by remember { mutableIntStateOf(1) }
    var isPublic by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val setMinPlayers= 2
    val setMaxPlayers= 8
    val setMinRounds = 1
    val setMaxRounds = 100
    val setMinAI     = 0

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate900, Slate800)))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BorderStroke(1.dp, Slate600),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text("Create Game", style = MaterialTheme.typography.headlineSmall)

                    Column {
                        Text("Number of Players: $maxPlayers", style = MaterialTheme.typography.bodyLarge)
                        Slider(
                            value = maxPlayers.toFloat(),
                            onValueChange = {
                                val newValue = it.toInt().coerceIn(setMinPlayers, setMaxPlayers)
                                maxPlayers = newValue
                                if (aiCount >= newValue) aiCount = newValue - 1
                            },
                            valueRange = setMinPlayers.toFloat()..setMaxPlayers.toFloat(),
                            steps = setMaxPlayers-setMinPlayers,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Emerald500,
                                activeTrackColor = Emerald500
                            )
                        )
                    }

                    Column {
                        Text("AI Opponents: $aiCount", style = MaterialTheme.typography.bodyLarge)
                        Slider(
                            value = aiCount.toFloat(),
                            onValueChange = {
                                aiCount = it.toInt().coerceIn(setMinAI, maxPlayers - 1)
                            },
                            valueRange = setMinAI.toFloat()..(maxPlayers - 1).toFloat(),
                            steps = (maxPlayers - 1).coerceAtLeast(1) - 1,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFACC15),
                                activeTrackColor = Color(0xFFFACC15)
                            )
                        )
                    }

                    Column {
                        Text("Rounds: $rounds", style = MaterialTheme.typography.bodyLarge)
                        Slider(
                            value = rounds.toFloat(),
                            onValueChange = {
                                rounds = it.toInt().coerceIn(setMinRounds, setMaxRounds)
                            },
                            valueRange = setMinRounds.toFloat()..setMaxRounds.toFloat(),
                            steps = setMaxRounds - setMinRounds,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Indigo500,
                                activeTrackColor = Indigo500
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Make Game Public")
                    }

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            onCreate(rounds, maxPlayers, aiCount, isPublic)
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(Emerald500, Emerald600)),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLoading) "Creating..." else "Create Game",
                                color = Color.White
                            )
                        }
                    }

                    TextButton(onClick = { if (!isLoading) onBack() }) {
                        Text("Back", color = Emerald500)
                    }
                }
            }
        }
    }
}



