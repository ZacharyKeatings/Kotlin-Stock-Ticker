package com.example.stockticker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockticker.ui.theme.*
import com.example.stockticker.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val cardColor = Slate800.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate900, Slate600)))
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
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    when {
                        ui.error != null -> {
                            Text(
                                text = "Error: ${ui.error}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        ui.username == null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        else -> {
                            ProfileItem("Username", ui.username)
                            ProfileItem("Email", ui.email)
                            ProfileItem("Joined", ui.joined)

                            Divider(Modifier.padding(vertical = 8.dp))

                            ProfileItem("Games Played", ui.gamesPlayed?.toString())
                            ProfileItem("Games Won", ui.gamesWon?.toString())
                            ProfileItem("Games Lost", ui.gamesLost?.toString())

                            Divider(Modifier.padding(vertical = 8.dp))

                            ProfileItem("Stocks Bought", ui.totalStocksBought?.toString())
                            ProfileItem("Stocks Sold", ui.totalStocksSold?.toString())
                            ProfileItem("Money Earned", ui.totalMoneyEarned?.let { "$%.2f".format(it) })
                            ProfileItem("Money Spent", ui.totalMoneySpent?.let { "$%.2f".format(it) })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onBack) {
                        Text("Back", color = Emerald500)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileItem(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Text("$label: $value", style = MaterialTheme.typography.bodyLarge)
    }
}
