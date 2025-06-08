// SettingsScreen.kt
package com.example.stockticker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stockticker.ui.theme.*

@Composable
fun SettingsScreen(
    username: String,
    token: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

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
                border = BorderStroke(1.dp, Slate600),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Settings", style = MaterialTheme.typography.headlineSmall)
                    Text("Logged in as: $username", style = MaterialTheme.typography.bodyLarge)

                    StyledButton(
                        text = "Log Out",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        showLogoutConfirm = true
                    }

                    TextButton(onClick = onBack) {
                        Text("Back", color = Emerald500)
                    }
                }
            }

            if (showLogoutConfirm) {
                AlertDialog(
                    onDismissRequest = { showLogoutConfirm = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutConfirm = false
                            onLogout()
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutConfirm = false }) {
                            Text("No")
                        }
                    },
                    title = { Text("Confirm Logout") },
                    text = { Text("Are you sure you want to log out?") },
                    containerColor = Slate800,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
        }
    }
}
