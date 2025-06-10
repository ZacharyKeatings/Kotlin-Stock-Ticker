
package com.example.stockticker

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.stockticker.auth.AuthManager
import com.example.stockticker.network.SocketManager
import com.example.stockticker.ui.StockTickerApp
import com.example.stockticker.ui.theme.StockTickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AuthManager.init(applicationContext)
        SocketManager.initializeSocket()
        setContent {
            StockTickerTheme {
                val appKey = remember { mutableStateOf(0) }

                key(appKey.value) {
                    StockTickerApp(
                        restartApp = {
                            appKey.value += 1
                        }
                    )
                }
            }
        }
    }
}