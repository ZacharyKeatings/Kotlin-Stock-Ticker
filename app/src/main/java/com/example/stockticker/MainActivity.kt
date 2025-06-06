
package com.example.stockticker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.stockticker.auth.AuthManager
import com.example.stockticker.network.SocketManager
import com.example.stockticker.ui.StockTickerApp
import com.example.stockticker.ui.theme.StockTickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.init(applicationContext)
        SocketManager.initializeSocket()
        setContent {
            StockTickerTheme {
                StockTickerApp()
            }
        }
    }
}