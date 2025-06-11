package com.example.stockticker.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.stockticker.network.SocketManager
import com.example.stockticker.ui.components.ingame.ActionHistory
import com.example.stockticker.ui.components.ingame.DiceRollHistoryDisplay
import com.example.stockticker.ui.components.ingame.GameHeader
import com.example.stockticker.ui.components.ingame.PlayerStats
import com.example.stockticker.ui.components.ingame.StockActionDialog
import com.example.stockticker.ui.components.ingame.StockBoard
import com.example.stockticker.ui.components.ingame.InGameActionBar
import com.example.stockticker.viewmodel.GameViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InGameScreen(
    gameId        : String,
    username      : String?,
    token         : String?,
    gameVm        : GameViewModel,
    onToast       : (String) -> Unit,
    onGameComplete: () -> Unit,
    onReturnHome  : () -> Unit
) {
    val socket   = SocketManager.getSocket()
    val socketId = socket.id()

    // Observe game state
    val uiState  by gameVm.state.collectAsState()
    val gameJson = uiState.game
    if (gameJson == null) {
        LoadingState()
        return
    }

    // Rejoin on resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && username != null) {
                Log.d("GameVM", "App resumed: rejoining $gameId as $username")
                gameVm.rejoinGame(gameId, username, token)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // One‐time toast
    uiState.toastMessage?.let { msg ->
        LaunchedEffect(msg) {
            onToast(msg)
            gameVm.clearToast()
        }
    }

    // Parse common fields
    val round        = gameJson.optInt("round", 1)
    val maxRounds    = gameJson.optInt("maxRounds", 1)
    val currentPid   = gameJson.optString("currentTurnPlayerId")
    val status       = gameJson.optString("status", "unknown").lowercase().trim()
    val stocksJson   = gameJson.optJSONObject("stocks") ?: JSONObject()
    val playersArray = gameJson.optJSONArray("players") ?: JSONArray()
    val historyArray = gameJson.optJSONArray("history") ?: JSONArray()
    val currentTurnPlayerId = gameJson.optString("currentTurnPlayerId")

    // Find my player object
    val localPlayerJson = (0 until playersArray.length())
        .map { playersArray.getJSONObject(it) }
        .firstOrNull { it.optString("id") == socketId }

    val myUsername      = localPlayerJson?.optString("username")
    val currentRoundNum = gameJson.optInt("round", 0)

    val isMyTurn = socketId == currentTurnPlayerId
    val hasRolled = localPlayerJson?.optBoolean("hasRolled", false) ?: false
    val hasStock = localPlayerJson
        ?.optJSONObject("portfolio")
        ?.let { port ->
            port.keys().asSequence().any { symbol ->
                val arr = port.optJSONArray(symbol) ?: return@any false
                (0 until arr.length()).any { i ->
                    arr.optJSONObject(i)?.optInt("qty",0) ?: 0 > 0
                }
            }
        } ?: false

    val availableCash = localPlayerJson?.optDouble("cash", 0.0) ?: 0.0
    val blockSize    = 500
    val canBuyBlock = stocksJson.keys().asSequence().any { symbol ->
        val price = stocksJson
            .optJSONObject(symbol)
            ?.optDouble("price", 0.0)
            ?: 0.0
        availableCash >= price * blockSize
    }

    val currentPlayerName = (0 until playersArray.length())
        .map { playersArray.getJSONObject(it) }
        .firstOrNull { it.optString("id") == currentPid }
        ?.optString("username")

    var actionMode    by remember { mutableStateOf<String?>(null) }
    var selectedStock by remember { mutableStateOf<String?>(null) }
    var quantity      by remember { mutableIntStateOf(0) }

    // Navigate to game over when complete
    LaunchedEffect(status) {
        if (status == "complete") onGameComplete()
    }

    // Tabs
    val tabs        = listOf("Market", "Players", "History")
    var selectedTab by remember { mutableStateOf(0) }

    // Pager state
    val pagerState      = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope  = rememberCoroutineScope()

    // Keep selectedTab in sync with pager
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                // Game header
                GameHeader(
                    round             = round,
                    maxRounds         = maxRounds,
                    currentPlayerName = currentPlayerName,
                )
                DiceRollHistoryDisplay(historyArray)
                // Tabs row
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text     = { Text(title) },
                            selected = selectedTab == index,
                            onClick  = {
                                // animate pager and update state
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }

                if (actionMode != null) {
                    StockActionDialog(
                        game             = gameJson,
                        visible          = true,
                        onDismiss        = {
                            actionMode    = null
                            selectedStock = null
                            quantity      = 0
                        },
                        mode             = actionMode,
                        selectedStock    = selectedStock,
                        setSelectedStock = { selectedStock = it },
                        quantity         = quantity,
                        setQuantity      = { quantity = it },
                        gameVm           = gameVm,
                        gameId           = gameId,
                        localPlayer      = localPlayerJson
                    )
                }
            }
        },
        bottomBar = {
            InGameActionBar(
                isMyTurn  = isMyTurn,
                isActive  = status == "active",
                isInitial = status == "initial-buy",
                hasRolled = hasRolled,
                canBuyBlock  = canBuyBlock,
                hasStock  = hasStock,
                onRoll    = { gameVm.handleRoll(gameId) },
                onBuy     = { actionMode = "buy" },
                onSell    = { actionMode = "sell" },
                onEnd     = { gameVm.endTurn(gameId) }
            )
        }
    ) { innerPadding ->
        // Wrap the three pages in a swipeable pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                // MARKET
                0 -> StockBoard(
                    stocks       = stocksJson,
                    stockChanges = uiState.stockChanges,
                    priceHistory = uiState.priceHistory,
                    modifier     = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                // PLAYERS
                1 -> PlayerStats(
                    players             = playersArray,
                    currentTurnPlayerId = currentPid,
                    stocks              = stocksJson,
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                // HISTORY
                2 -> ActionHistory(
                    historyJson   = historyArray,
                    currentRound  = currentRoundNum,
                    localUsername = myUsername
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(8.dp))
        Text("Loading…", style = MaterialTheme.typography.bodyLarge)
    }
}


