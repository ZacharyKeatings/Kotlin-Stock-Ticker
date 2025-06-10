package com.example.stockticker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockticker.network.SocketManager
import io.socket.client.Ack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Holds all in-game UI state.  Whenever ANY of these fields updates,
 * Compose recomposes all consumers of GameUiState.
 */
data class GameUiState(
    // The full game JSON as received from server's `game:update` event:
    val game: JSONObject? = null,

    // Map from stock symbol → last action ("up", "down", or "dividend").
    val stockChanges: Map<String, String> = emptyMap(),

    // Map from stock symbol → list of recent Double prices (for sparkline).
    val priceHistory: Map<String, List<Double>> = emptyMap(),

    // The last dice‐roll payload, as received from `game:diceRolled`.
    val lastRoll: JSONObject? = null,

    // Any one-time toast (e.g. server error, or “Player X rolled Y”). Cleared after UI consumes it.
    val toastMessage: String? = null,

    // If the server is sending a countdown (seconds remaining), store it here.
    val countdown: Int? = null,

    // In case server emits finalResults at game end, we can hold them here if needed.
    // val finalResults: JSONArray? = null
)

class GameViewModel : ViewModel() {

    // Backing state flow:
    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    // Shared Socket.IO instance:
    private val socket = SocketManager.getSocket()

    init {
        // ─── Listen for “game:update” ────────────────────────────────────
        //
        // The server’s GameManager.broadcastGameState() emits “game:update” with a payload:
        // {
        //   id, round, maxRounds, stocks, players, maxPlayers,
        //   currentTurnPlayerId, status, history (last 30 entries)
        // }
        socket.on("game:update") { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@on
            _state.update { old ->
                // Replace the entire game JSON. UI can read stocks, players, status, etc.
                old.copy(game = payload)
            }
        }

        // ─── Listen for “game:diceRolled” ───────────────────────────────
        socket.on("game:diceRolled") { args ->
            val rawPayload = args.firstOrNull() ?: return@on

            // Handle both JSONObject (ACK) and Map<String, Any> (server emit)
            val rollJson = when (rawPayload) {
                is JSONObject -> rawPayload
                is Map<*, *>  -> JSONObject(rawPayload)
                else           -> return@on
            }

            handleDiceRolled(rollJson)
        }

        // ─── Listen for “game:toast” (one-time messages from server) ────
        socket.on("game:toast") { args ->
            val js = args.firstOrNull() as? JSONObject
            val msg = js?.optString("message")
            if (!msg.isNullOrBlank()) {
                _state.update { old ->
                    old.copy(toastMessage = msg)
                }
            }
        }

        // ─── Listen for “game:clearRoll” ────────────────────────────────
//        socket.on("game:clearRoll") {
//            viewModelScope.launch {
//                delay(300)  // give Compose a frame or two to render the rollText
//                _state.update { old -> old.copy(lastRoll = null) }
//            }
//        }

        // ─── Listen for “game:countdown” and “game:countdownCancelled” ─
        socket.on("game:countdown") { args ->
            val value = args.firstOrNull() as? Int
            _state.update { old ->
                old.copy(countdown = value)
            }
        }
        socket.on("game:countdownCancelled") {
            _state.update { old ->
                old.copy(countdown = null)
            }
        }

        // ─── (OPTIONAL) Listen for “game:finalResults” if you want to capture final leaderboard ─
        // socket.on("game:finalResults") { args ->
        //     val results = args.firstOrNull() as? JSONArray ?: return@on
        //     _state.update { old ->
        //         old.copy(finalResults = results)
        //     }
        // }
    }

    /**
     * Called whenever the server emits “game:diceRolled”.  We:
     *  1. Update lastRoll (for “Rolled: STOCK ACTION AMOUNT” UI).
     *  2. Update stockChanges[stock] = action (so StockBoard can color the row).
     *  3. Append the new price (pulled from state.game.stocks) into priceHistory[stock].
     * Compose sees a brand-new immutable GameUiState and recomposes everything.
     */
    private fun handleDiceRolled(rollJson: JSONObject) {
        val stockSymbol = rollJson.optString("stock")
        val action = rollJson.optString("action") // “up” / “down” / “dividend”

        // 1) lastRoll
        _state.update { old ->
            old.copy(lastRoll = rollJson)
        }

        // 2) stockChanges
        _state.update { old ->
            old.copy(stockChanges = old.stockChanges + (stockSymbol to action))
        }

        // 3) priceHistory
        //    We expect that _state.value.game already holds the latest game JSON,
        //    which includes stocks: { Gold: { price: 1.10 }, … }.  If not, default to 1.0.
        val currentGame = _state.value.game
        val newPrice: Double = currentGame
            ?.optJSONObject("stocks")
            ?.optJSONObject(stockSymbol)
            ?.optDouble("price", 1.0)
            ?: 1.0

        _state.update { old ->
            val existingList = old.priceHistory[stockSymbol].orEmpty()
            val updatedList = (existingList + newPrice).takeLast(8)
            old.copy(priceHistory = old.priceHistory + (stockSymbol to updatedList))
        }
    }

    /**
     * Called by the UI when the local (human) player clicks “Roll Dice.”
     * This emits “game:roll” to the server; the server should reply by broadcasting
     * “game:diceRolled” (which we listen for above).  If the server responds in
     * an ACK rather than separate event, we catch errors here.
     */
    fun handleRoll(gameId: String) {
        viewModelScope.launch {
            val payload = JSONObject().put("gameId", gameId)
            socket.emit("game:roll", payload, io.socket.client.Ack { ackArgs ->
                val first = ackArgs.firstOrNull()
                if (first is JSONObject) {
                    val success = first.optBoolean("success", false)
                    if (!success) {
                        val errorMsg = first.optString("error", "Roll failed")
                        _state.update { old -> old.copy(toastMessage = errorMsg) }
                    } else {
                        // If server immediately returns the `roll` inside this ACK:
                        val returnedRoll = first.optJSONObject("roll")
                        if (returnedRoll != null) {
                            handleDiceRolled(returnedRoll)
                        }
                    }
                }
            })
        }
    }

    /**
     * Called by UI when the human clicks “Buy Stock.”  Emits “game:buy”.
     * Server will respond by updating game state and emitting “game:toast” or “game:update”.
     */
    fun buyStock(gameId: String, stock: String, quantity: Int) {
        viewModelScope.launch {
            val payload = JSONObject()
                .put("gameId", gameId)
                .put("stock", stock)
                .put("quantity", quantity)
            socket.emit("game:buy", payload, io.socket.client.Ack { ackArgs ->
                val first = ackArgs.firstOrNull()
                if (first is JSONObject) {
                    val success = first.optBoolean("success", false)
                    if (!success) {
                        val errorMsg = first.optString("error", "Buy failed")
                        _state.update { old -> old.copy(toastMessage = errorMsg) }
                    }
                }
            })
        }
    }

    /**
     * Called by UI when the human clicks “Sell Stock.” Emits “game:sell”.
     * Server will broadcast “game:update” and/or “game:toast” after processing.
     */
    fun sellStock(gameId: String, stock: String, quantity: Int) {
        viewModelScope.launch {
            val payload = JSONObject()
                .put("gameId", gameId)
                .put("stock", stock)
                .put("quantity", quantity)
            socket.emit("game:sell", payload, io.socket.client.Ack { ackArgs ->
                val first = ackArgs.firstOrNull()
                if (first is JSONObject) {
                    val success = first.optBoolean("success", false)
                    if (!success) {
                        val errorMsg = first.optString("error", "Sell failed")
                        _state.update { old -> old.copy(toastMessage = errorMsg) }
                    }
                }
            })
        }
    }

    /**
     * Called by UI when the human clicks “End Turn.” Emits “game:endTurn”.
     * If server rejects (e.g. “not your turn”), we show a toast. Server will
     * broadcast “game:clearRoll” and “game:update” and possibly advance to AI turn.
     */
    fun endTurn(gameId: String) {
        viewModelScope.launch {
            val payload = JSONObject().put("gameId", gameId)
            socket.emit("game:endTurn", payload, io.socket.client.Ack { ackArgs ->
                val first = ackArgs.firstOrNull()
                if (first is JSONObject) {
                    val success = first.optBoolean("success", false)
                    if (!success) {
                        val errorMsg = first.optString("error", "Cannot end turn")
                        _state.update { old -> old.copy(toastMessage = errorMsg) }
                    }
                }
            })
        }
    }

    /**
     * Called by UI when the room-owner clicks “Start Game.” Emits “game:start”.
     * Server will broadcast “game:update” and begin countdown if successful.
     */
    fun startGame(gameId: String) {
        viewModelScope.launch {
            val payload = JSONObject().put("gameId", gameId)
            socket.emit("game:start", payload, io.socket.client.Ack { ackArgs ->
                val first = ackArgs.firstOrNull()
                if (first is JSONObject) {
                    val success = first.optBoolean("success", false)
                    if (!success) {
                        val errorMsg = first.optString("error", "Start failed")
                        _state.update { old -> old.copy(toastMessage = errorMsg) }
                    }
                }
            })
        }
    }

    /**
     * Called by UI when a new player wants to join. Emits “game:join”.
     * On success, server will broadcast “game:update” to all. On failure,
     * server ACKs with { success: false, message: ... }.
     */
    fun joinGame(
        gameId: String,
        username: String?,
        token: String?,
        onSuccess: () -> Unit = {},
        onError: (errorMessage: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val payload = JSONObject().apply {
                put("gameId", gameId)
                put("username", username)
                put("token", token)
            }
            socket.emit("game:join", payload, Ack { ackArgs ->
                val first = ackArgs.firstOrNull() as? JSONObject
                val ok = first?.optBoolean("success", false) ?: false
                if (ok) {
                    // fire the optional callback on the Main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    val msg = first?.optString("message") ?: "Join failed"
                    viewModelScope.launch(Dispatchers.Main) {
                        onError(msg)
                    }
                    // also queue a toast so that hosts get notified
                    _state.update { it.copy(toastMessage = msg) }
                }
            })
        }
    }

    /**
     * Called by UI when reconnecting/rejoining a game. Emits “game:rejoin”.
     */
    fun rejoinGame(gameId: String, username: String, token: String?) {
        viewModelScope.launch {
            val payload = JSONObject()
                .put("gameId", gameId)
                .put("username", username)
                .put("token", token)
            socket.emit("game:rejoin", payload, io.socket.client.Ack { ackArgs ->
                val first = ackArgs.firstOrNull() as? JSONObject
                if (first != null) {
                    val success = first.optBoolean("success", false)
                    if (!success) {
                        val errorMsg = first.optString("message", "Rejoin failed")
                        _state.update { old -> old.copy(toastMessage = errorMsg) }
                    }
                }
            })
        }
    }

    /**
     * Completely resets the in‐game UI state back to “no game.”  Also
     * unregisters any socket event handlers that were tied to the old game.
     */
    fun clearGameState() {
        // 1) Reset the UI state
        _state.value = GameUiState()

        // 2) Turn off any listeners that were attached in `init { … }`.
        //    (If you used `socket.on("game:update") { … }` etc. in init, remove them here.)
//        socket.off("game:update")
        socket.off("game:diceRolled")
        socket.off("game:toast")
//        socket.off("game:clearRoll")
        socket.off("game:countdown")
        socket.off("game:countdownCancelled")
        // (If you ever listen for "game:finalResults", turn that off too.)

        // Now, when you call `clearGameState()`, the VM will behave as though
        // “no game is currently in progress” and no old countdowns or updates
        // can come through.
    }

    /**
     * Called by UI when leaving a game early (“return home”). Emits “game:returnHome”.
     * Server will clean up and remove the game. Typically UI will navigate away.
     */
    fun returnHome(gameId: String) {
        viewModelScope.launch {
            val payload = JSONObject().put("gameId", gameId)
            socket.emit("game:returnHome", payload)
        }
    }

    /**
     * One-time clear of toast message after UI shows it.
     */
    fun clearToast() {
        _state.update { old ->
            old.copy(toastMessage = null)
        }
    }

    /**
     * Clean up socket listeners when ViewModel is destroyed.
     */
    override fun onCleared() {
        socket.off("game:update")
        socket.off("game:diceRolled")
        socket.off("game:toast")
//        socket.off("game:clearRoll")
        socket.off("game:countdown")
        socket.off("game:countdownCancelled")
        // socket.off("game:finalResults")
        super.onCleared()
    }

    /**
     * Creates a new game by emitting "game:create" to the server.
     *
     * Now ensures that onSuccess/onError are always called on the MAIN thread,
     * so that any navigation or Compose state updates happen safely.
     */
    fun createGame(
        rounds: Int,
        maxPlayers: Int,
        aiCount: Int,
        isPublic: Boolean,
        username: String?,
        token: String?,
        onSuccess: (newGameId: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        viewModelScope.launch {
            // Build the JSON payload
            val payload = JSONObject().apply {
                put("rounds", rounds)
                put("maxPlayers", maxPlayers)
                put("aiCount", aiCount)
                put("isPublic", isPublic)
                put("username", username) // null becomes JSON null
                put("token", token)       // null becomes JSON null
            }

            // Emit "game:create" and provide an Ack callback.
            //
            // IMPORTANT: Socket acknowledges on a background thread. We must switch to
            // the Main dispatcher before calling onSuccess/onError.
            val socket = SocketManager.getSocket()
            socket.emit(
                "game:create",
                payload,
                Ack { args: Array<Any> ->
                    // This lambda runs on Socket.IO's EventThread (not the UI thread).
                    val first = args.firstOrNull() as? JSONObject
                    if (first != null) {
                        val success = first.optBoolean("success", false)
                        if (success) {
                            val gameId = first.optString("gameId", null)
                            if (!gameId.isNullOrEmpty()) {
                                // Switch back to Main thread before calling onSuccess:
                                viewModelScope.launch(Dispatchers.Main) {
                                    onSuccess(gameId)
                                }
                            } else {
                                viewModelScope.launch(Dispatchers.Main) {
                                    onError("Server did not return a valid Game ID.")
                                }
                            }
                        } else {
                            val message = first.optString("message", "Failed to create game.")
                            viewModelScope.launch(Dispatchers.Main) {
                                onError(message)
                            }
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.Main) {
                            onError("Unexpected server response.")
                        }
                    }
                }
            )
        }
    }
}
