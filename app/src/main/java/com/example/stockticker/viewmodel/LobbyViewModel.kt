package com.example.stockticker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockticker.network.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds only the “countdown” state for the lobby.
 * Listens for "game:countdown" and "game:countdownCancelled" events.
 */
data class LobbyState(
    val countdown: Int? = null
)

class LobbyViewModel : ViewModel() {

    private val _state = MutableStateFlow(LobbyState())
    val state: StateFlow<LobbyState> = _state.asStateFlow()

    private val socket = SocketManager.getSocket()

    init {
        // Listen for the server’s countdown events:
        socket.on("game:countdown") { args ->
            val seconds = args.firstOrNull() as? Int
            _state.update { old -> old.copy(countdown = seconds) }
        }

        socket.on("game:countdownCancelled") {
            _state.update { old -> old.copy(countdown = null) }
        }
    }

    override fun onCleared() {
        // Unregister the listeners when ViewModel is destroyed:
        socket.off("game:countdown")
        socket.off("game:countdownCancelled")
        super.onCleared()
    }
}
