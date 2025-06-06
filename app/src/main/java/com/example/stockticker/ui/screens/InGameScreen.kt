// InGameScreen.kt
package com.example.stockticker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockticker.network.SocketManager
import com.example.stockticker.ui.components.ingame.*
import com.example.stockticker.viewmodel.GameViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun InGameScreen(
    gameId       : String,
    socketId     : String,
    username     : String?,
    token        : String?,
    gameVm       : GameViewModel,
    onToast      : (String) -> Unit,
    onReturnHome : () -> Unit
) {
    // 1) Observe the ViewModel’s state (full game JSON, lastRoll, toast, etc.)
    val uiState by gameVm.state.collectAsState()
    val gameJson = uiState.game

    // 2) If there's a one‐time toast, show it then clear it
    uiState.toastMessage?.let { msg ->
        LaunchedEffect(msg) {
            onToast(msg)
            gameVm.clearToast()
        }
    }

    // 3) While waiting for the first "game:update", show a loading spinner
    if (gameJson == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Loading game…", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    // 4) Pull out the fields we need from gameJson
    val round           = gameJson.optInt("round", 1)
    val maxRounds       = gameJson.optInt("maxRounds", 1)
    val currentPid      = gameJson.optString("currentTurnPlayerId")
    val rawStatus       = gameJson.optString("status", "unknown")
    val status          = rawStatus.trim().lowercase()      // normalize
    val stocksJson      = gameJson.optJSONObject("stocks") ?: JSONObject()
    val playersArray    = gameJson.optJSONArray("players")  ?: return

    val isInitialBuy = (status == "initial-buy")
    val isActive     = (status == "active")
    val isMyTurn     = (currentPid == socketId)

    // 5) Find this client’s player object, so we can compute hasMoney / hasAnyStock
    val localPlayerJson = (0 until playersArray.length())
        .map { playersArray.getJSONObject(it) }
        .find { it.optString("id") == socketId }

    val availableCash = localPlayerJson?.optDouble("cash", 0.0) ?: 0.0
    val portfolioJson = localPlayerJson?.optJSONObject("portfolio") ?: JSONObject()
    val hasAnyStock = portfolioJson.keys()
        .asSequence()
        .any { key -> portfolioJson.optInt(key, 0) > 0 }
    val hasMoney = (availableCash > 0.0)

    // 6) Figure out whose turn it is (their username)
    val currentPlayerName = (0 until playersArray.length())
        .map { playersArray.getJSONObject(it) }
        .find { it.optString("id") == currentPid }
        ?.optString("username")

    // 7) Local "hasRolled" flag:
    //    - We set hasRolled = true as soon as the user taps “Roll Dice.”
    //    - When the server emits “game:clearRoll” (i.e. uiState.lastRoll == null), reset hasRolled = false.
    var hasRolled by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.lastRoll) {
        if (uiState.lastRoll == null) {
            hasRolled = false
        }
    }

    // 7.b) ALSO, whenever the round or the current player changes, and it's now my new turn,
    //       reset hasRolled = false. This covers round‐boundary cases.
    LaunchedEffect(round, currentPid) {
        if (isMyTurn) {
            hasRolled = false
        }
    }

    // 8) Local state for the Buy/Sell dialog
    var actionMode    by remember { mutableStateOf<String?>(null) }
    var selectedStock by remember { mutableStateOf<String?>(null) }
    var quantity      by remember { mutableIntStateOf(0) }

    // 9) We read stockChanges & priceHistory directly out of the ViewModel's state
    val stockChanges = uiState.stockChanges
    val priceHistory = uiState.priceHistory

    // 10) If the game transitions to "complete", immediately navigate home
    LaunchedEffect(status) {
        if (status == "complete") {
            onReturnHome()
        }
    }

    // 11) Now build a Scaffold so that the bottom‐bar does not overlap the scrollable content
    Scaffold(
        bottomBar = {
            // A column for two rows of buttons (2×2 grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                // ─ Top row: 🎲 Roll Dice  |  🟢 Buy Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Roll Dice (top‐left)
                    Button(
                        onClick = {
                            gameVm.handleRoll(gameId)
                            hasRolled = true
                        },
                        enabled = isMyTurn && !hasRolled && isActive,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("🎲 Roll Dice")
                    }

                    // Buy Stock (top‐right)
                    Button(
                        onClick = { actionMode = "buy" },
                        enabled = isMyTurn && (isInitialBuy || (isActive && hasRolled)) && hasMoney,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("🟢 Buy Stock")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ─ Bottom row: 🔚 End Turn  |  🔴 Sell Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // End Turn (bottom‐left)
                    Button(
                        onClick = { gameVm.endTurn(gameId) },
                        enabled = isMyTurn && (isInitialBuy || (isActive && hasRolled)),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("🔚 End Turn")
                    }

                    // Sell Stock (bottom‐right)
                    Button(
                        onClick = { actionMode = "sell" },
                        enabled = isMyTurn && (isInitialBuy || (isActive && hasRolled)) && hasAnyStock,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("🔴 Sell Stock")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 12.A) Game Header
            item {
                GameHeader(
                    round = round,
                    maxRounds = maxRounds,
                    currentPlayerName = currentPlayerName,
                    status = status
                )
            }

            // 12.B) StockBoard (2×3 grid)
            item {
                StockBoard(
                    stocks = stocksJson,
                    stockChanges = stockChanges,
                    priceHistory = priceHistory
                )
            }

            // 12.C) Player Stats
            item {
                PlayerStats(
                    players = playersArray,
                    currentTurnPlayerId = currentPid,
                    stocks = stocksJson
                )
            }

            // 12.D) “Last Roll” Banner (if any)
            item {
                uiState.lastRoll?.let { rollJson ->
                    val stockSymbol = rollJson.optString("stock", "").uppercase()
                    val action      = rollJson.optString("action", "").uppercase()
                    val amount      = rollJson.optInt("amount", 0)

                    Text(
                        text = "🎲 Rolled: $stockSymbol $action $amount",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        // 13) Show the Buy/Sell dialog when actionMode != null
        if (actionMode != null) {
            StockActionDialog(
                game            = gameJson,
                visible         = true,
                onDismiss       = {
                    actionMode    = null
                    selectedStock = null
                    quantity      = 0
                },
                mode            = actionMode,
                selectedStock   = selectedStock,
                setSelectedStock = { selectedStock = it },
                quantity        = quantity,
                setQuantity     = { quantity = it },
                gameVm          = gameVm,
                gameId          = gameId,
                localPlayer     = localPlayerJson
            )
        }
    }
}
