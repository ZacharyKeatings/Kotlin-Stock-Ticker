package com.example.stockticker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockticker.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    // 1) pull in your UI state
    val ui by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)          // respect the top bar
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            when {
                ui.error != null -> {
                    Text(
                        text = "Error: ${ui.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                ui.username == null -> {
                    // still loading
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
                    // profile info
                    Text("Username: ${ui.username}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("Email:    ${ui.email}",    style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("Joined:   ${ui.joined}",   style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(24.dp))

                    // basic stats
                    Text("Games Played:      ${ui.gamesPlayed}", style = MaterialTheme.typography.bodyLarge)
                    Text("Games Won:         ${ui.gamesWon}",    style = MaterialTheme.typography.bodyLarge)
                    Text("Games Lost:        ${ui.gamesLost}",   style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(24.dp))

                    // optional extra stats
                    ui.totalStocksBought?.let {
                        Text("Stocks Bought:     $it", style = MaterialTheme.typography.bodyLarge)
                    }
                    ui.totalStocksSold?.let {
                        Text("Stocks Sold:       $it", style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.height(24.dp))

                    ui.totalMoneyEarned?.let {
                        Text(
                            "Money Earned: \$${"%.2f".format(it)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    ui.totalMoneySpent?.let {
                        Text(
                            "Money Spent:  \$${"%.2f".format(it)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
