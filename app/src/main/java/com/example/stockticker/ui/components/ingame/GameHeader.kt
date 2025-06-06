package com.example.stockticker.ui.components.ingame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameHeader(
    round: Int,
    maxRounds: Int,
    currentPlayerName: String?,
    status: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Round $round / $maxRounds",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        currentPlayerName?.let {
            Text(
                text = "🎯 Current Turn: $it",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Game Status: ${status.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.labelMedium
        )
    }
}