package com.example.stockticker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stockticker.ui.theme.*

@Composable
fun HomeScreen(
    username: String,
    token: String,
    lastGameId: String?,
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    onFindGame: () -> Unit,
    onRejoinGame: ((gameId: String) -> Unit)?,
    onProfile: (() -> Unit)?,
    onSettings: (() -> Unit)?
) {
    val gradientBg = Brush.verticalGradient(listOf(Slate900, Slate800))
    val cardColor = Slate800.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gradient Title
                    Text(
                        text = "Stock Ticker",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Indigo500, Fuchsia500, Pink500)
                            )
                        )
                    )

                    Text(
                        text = "Welcome, $username",
                        color = Slate400,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Buttons
                    StyledButton("Create Game", onClick = onCreateGame)
                    StyledButton("Join Game by ID", onClick = onJoinGame)
                    StyledButton("Find Public Games", onClick = onFindGame)

                    if (!lastGameId.isNullOrBlank() && onRejoinGame != null) {
                        StyledButton("Rejoin My Game") {
                            onRejoinGame(lastGameId)
                        }
                    }

                    if (onProfile != null || onSettings != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            onProfile?.let {
                                StyledButton(
                                    text = "Profile",
                                    modifier = Modifier.weight(1f),
                                    onClick = it
                                )
                            }
                            onSettings?.let {
                                StyledButton(
                                    text = "Settings",
                                    modifier = Modifier.weight(1f),
                                    onClick = it
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StyledButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Slate700,
            disabledContentColor = Slate300
        ),
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
            Text(text = text, color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}
