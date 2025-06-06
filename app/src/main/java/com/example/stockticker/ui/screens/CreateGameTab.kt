package com.example.stockticker.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.example.stockticker.network.SocketManager
//import io.socket.client.Ack
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//
//@Composable
//fun CreateGameTab(
//    username: String,
//    token: String?,
//    onGameCreated: (gameId: String) -> Unit = {}
//) {
//    var maxPlayers by remember { mutableIntStateOf(2) }
//    var rounds by remember { mutableIntStateOf(1) }
//    var aiCount by remember { mutableIntStateOf(1) }
//    var isPublic by remember { mutableStateOf(true) }
//    var isLoading by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    val coroutineScope = rememberCoroutineScope()
//
//    val socket = SocketManager.getSocket()
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("Create New Game", style = MaterialTheme.typography.headlineSmall)
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text("Players: $maxPlayers")
//        Slider(
//            value = maxPlayers.toFloat(),
//            onValueChange = {
//                maxPlayers = it.toInt().coerceIn(2, 8)
//                if (aiCount >= maxPlayers) aiCount = maxPlayers - 1
//            },
//            valueRange = 2f..8f,
//            steps = 6
//        )
//
//        Text("AI Opponents: $aiCount")
//        Slider(
//            value = aiCount.toFloat(),
//            onValueChange = { aiCount = it.toInt().coerceIn(0, maxPlayers - 1) },
//            valueRange = 0f..(maxPlayers - 1).toFloat(),
//            steps = maxPlayers - 2
//        )
//
//        Text("Rounds: $rounds")
//        Slider(
//            value = rounds.toFloat(),
//            onValueChange = { rounds = it.toInt().coerceIn(1, 100) },
//            valueRange = 1f..100f,
//            steps = 99
//        )
//
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Switch(
//                checked = isPublic,
//                onCheckedChange = { isPublic = it }
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text("Make Game Public")
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        errorMessage?.let {
//            Text(it, color = MaterialTheme.colorScheme.error)
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//
//        Button(
//            onClick = {
//                isLoading = true
//                errorMessage = null
//
//                socket.emit("game:create", JSONObject().apply {
//                    put("rounds", rounds)
//                    put("maxPlayers", maxPlayers)
//                    put("aiCount", aiCount)
//                    put("username", username)
//                    put("isPublic", isPublic)
//                    put("token", token)
//                }, Ack { args ->
//                    isLoading = false
//                    val response = args.firstOrNull() as? JSONObject
//                    val success = response?.optBoolean("success") == true
//                    val gameId = response?.optString("gameId")
//
//                    if (success && !gameId.isNullOrEmpty()) {
//                        coroutineScope.launch {
//                            onGameCreated(gameId)
//                        }
//                    } else {
//                        errorMessage = response?.optString("message") ?: "Game creation failed."
//                    }
//                })
//            },
//            modifier = Modifier.fillMaxWidth(),
//            enabled = !isLoading
//        ) {
//            Text(if (isLoading) "Creating..." else "Create Game")
//        }
//    }
//}
