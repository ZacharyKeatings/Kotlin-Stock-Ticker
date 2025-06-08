// GameListScreen.kt
package com.example.stockticker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stockticker.network.SocketManager
import io.socket.client.Ack
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject

private data class PublicGame(
    val id: String,
    val players: Int,
    val maxPlayers: Int,
    val round: Int,
    val status: String
)

@Composable
fun GameListScreen(
    username: String,
    token: String,
    onJoin: (gameId: String) -> Unit,
    onBack: () -> Unit
) {
    val socket = remember { SocketManager.getSocket() }

    // State backing the list of public games
    val games = remember { mutableStateListOf<PublicGame>() }

    // Helper to apply an incoming update
    fun updateGame(update: PublicGame) {
        val idx = games.indexOfFirst { it.id == update.id }
        if (update.status != "waiting") {
            // only show waiting games
            if (idx >= 0) games.removeAt(idx)
        } else {
            if (idx >= 0) games[idx] = update
            else games += update
        }
    }

    DisposableEffect(socket) {
        // 1) Request initial list
        socket.emit("game:listPublic", Ack { args ->
            val res = args.firstOrNull() as? JSONObject ?: return@Ack
            val arr = res.optJSONArray("games") ?: return@Ack
            val list = mutableListOf<PublicGame>()
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { g ->
                    list += PublicGame(
                        id         = g.optString("id"),
                        players    = g.optInt("players"),
                        maxPlayers = g.optInt("maxPlayers"),
                        round      = g.optInt("round"),
                        status     = g.optString("status")
                    )
                }
            }
            games.clear()
            games += list.filter { it.status == "waiting" }
        })

        val listener = fun(args: Array<Any>) {
            val u = args.firstOrNull() as? JSONObject ?: return
            val pg = PublicGame(
                id         = u.optString("id"),
                players    = u.optInt("players"),
                maxPlayers = u.optInt("maxPlayers"),
                round      = u.optInt("round"),
                status     = u.optString("status")
            )
            updateGame(pg)
        }
        socket.on("game:publicUpdated", listener)

        onDispose {
            socket.off("game:publicUpdated", listener)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding()
        ) {
            Text("Join a Public Game", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            if (games.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    Text("No public games available", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games) { g ->
                        PublicGameRow(game = g, onJoin = onJoin)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun PublicGameRow(game: PublicGame, onJoin: (String) -> Unit) {
    // status chip coloring
    val (bg, fg) = when (game.status) {
        "active"  -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        else      -> MaterialTheme.colorScheme.primaryContainer   to MaterialTheme.colorScheme.primary
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Game ID: ${game.id}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "Players: ${game.players}/${game.maxPlayers}  •  Round: ${game.round}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.width(8.dp))

        // status chip
        Surface(
            color = bg,
            shape = MaterialTheme.shapes.small,
            tonalElevation = 1.dp
        ) {
            Text(
                game.status.replaceFirstChar { it.uppercase() },
                color = fg,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.width(8.dp))

        Button(onClick = { onJoin(game.id) }) {
            Text("Join")
        }
    }
}
