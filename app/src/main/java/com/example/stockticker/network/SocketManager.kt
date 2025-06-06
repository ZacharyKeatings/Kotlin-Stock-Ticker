package com.example.stockticker.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {

    private const val SERVER_URL = "https://ticker.cinefiles.dev"
    private const val SOCKET_PATH = "/socket.io"

    private lateinit var socket: Socket

    fun initializeSocket() {
        try {
            val opts = IO.Options().apply {
                path = SOCKET_PATH
                transports = arrayOf("websocket")
                forceNew = true
                reconnection = true
            }

            socket = IO.socket(SERVER_URL, opts)

            socket.on(Socket.EVENT_CONNECT) {
                Log.d("Socket", "🟢 Connected: ${socket.id()}")
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                Log.d("Socket", "🔴 Disconnected")
            }

            socket.connect()

        } catch (e: Exception) {
            Log.e("Socket", "⚠️ Error initializing socket", e)
        }
    }

    fun getSocket(): Socket {
        return socket
    }

    fun isConnected(): Boolean {
        return ::socket.isInitialized && socket.connected()
    }

    fun disconnect() {
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }
}
