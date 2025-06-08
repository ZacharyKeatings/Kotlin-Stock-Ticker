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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stockticker.ui.theme.*
import androidx.compose.ui.graphics.Brush

/**
 * Shown when the user is not logged in. If the user is already authenticated,
 * the NavHost should skip this screen and navigate directly to HomeScreen.
 */
@Composable
fun StartScreen(
    onPlayAsGuest: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate900, Slate800)))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BorderStroke(1.dp, Slate600)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to StockTicker",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    StyledButton(
                        text = "Play as Guest",
                        onClick = onPlayAsGuest,
                        modifier = Modifier.fillMaxWidth()
                    )

                    StyledButton(
                        text = "Login",
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    StyledButton(
                        text = "Register",
                        onClick = onRegisterClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen(
        onPlayAsGuest = {},
        onLoginClick = {},
        onRegisterClick = {}
    )
}
