package com.example.stockticker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Shown when the user is not logged in. If the user is already authenticated,
 * the NavHost should skip this screen and navigate directly to HomeScreen.
 *
 * @param onPlayAsGuest    Called when “Play as Guest” is tapped.
 * @param onLoginClick     Called when “Login” is tapped.
 * @param onRegisterClick  Called when “Register” is tapped.
 */
@Composable
fun StartScreen(
    onPlayAsGuest: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to StockTicker",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onPlayAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Play as Guest")
            }

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Login")
            }

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Register")
            }
        }
    }
}

/**
 * Preview function for StartScreen.
 * Supplies no-op lambdas so that Android Studio can render it.
 */
@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen(
        onPlayAsGuest = {},
        onLoginClick = {},
        onRegisterClick = {}
    )
}
