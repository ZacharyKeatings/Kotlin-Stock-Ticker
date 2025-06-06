package com.example.stockticker.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.example.stockticker.network.SocketManager
//import org.json.JSONObject
//import io.socket.client.Ack
//
//@Composable
//fun GameListTab(
//    onJoinPublicGame: (PublicGame) -> Unit = {}
//) {
//    val socket = SocketManager.getSocket()
//    val publicGames = remember { mutableStateListOf<PublicGame>() }
//
//    // Initial fetch
//    LaunchedEffect(Unit) {
//        if (SocketManager.isConnected()) {
//            socket.emit("game:listPublic", Ack { args ->
//                val data = args.firstOrNull() as? JSONObject ?: return@Ack
//                val games = data.optJSONArray("games") ?: return@Ack
//                publicGames.clear()
//                for (i in 0 until games.length()) {
//                    val g = games.getJSONObject(i)
//                    publicGames.add(
//                        PublicGame(
//                            id = g.optString("id"),
//                            players = g.optInt("players"),
//                            maxPlayers = g.optInt("maxPlayers"),
//                            round = g.optInt("round"),
//                            status = g.optString("status")
//                        )
//                    )
//                }
//            })
//        }
//    }
//
//    // Realtime update listener
//    DisposableEffect(Unit) {
//        val listener: (Array<Any>) -> Unit = { args ->
//            val g = args.firstOrNull() as? JSONObject
//            if (g != null) {
//                val updatedGame = PublicGame(
//                    id = g.optString("id"),
//                    players = g.optInt("players"),
//                    maxPlayers = g.optInt("maxPlayers"),
//                    round = g.optInt("round"),
//                    status = g.optString("status")
//                )
//
//                publicGames.removeAll { it.id == updatedGame.id }
//
//                if (updatedGame.status == "waiting") {
//                    publicGames.add(updatedGame)
//                }
//            }
//        }
//
//        socket.on("game:publicUpdated", listener)
//
//        onDispose {
//            socket.off("game:publicUpdated", listener)
//        }
//    }
//
//    Column(modifier = Modifier
//        .fillMaxSize()
//        .padding(16.dp)) {
//
//        Text(
//            text = "Public Games",
//            style = MaterialTheme.typography.headlineSmall,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            items(publicGames) { game ->
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .padding(16.dp)
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Column {
//                            Text("Game ID: ${game.id}", style = MaterialTheme.typography.bodyLarge)
//                            Text("Players: ${game.players}/${game.maxPlayers} — Round: ${game.round}")
//                        }
//                        Button(onClick = { onJoinPublicGame(game) }) {
//                            Text("Join")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
