package com.example.stockticker.ui

import androidx.compose.runtime.*
import com.example.stockticker.auth.AuthManager
import com.example.stockticker.auth.UserIdentity
import com.example.stockticker.ui.navigation.AppNavHost

/**
 * Root Composable for the app.
 *
 * On first composition, it asks AuthManager.getCurrentUser() to figure out:
 *  • If the stored token is valid (a registered user) → startDestination = "home/{username}/{token}"
 *  • Otherwise (fallback to a guest) → startDestination = "start"
 */
@Composable
fun StockTickerApp(
    restartApp: () -> Unit = {}
) {
    val (user, setUser) = remember { mutableStateOf<UserIdentity?>(null) }
    val (startDestination, setStartDestination) = remember { mutableStateOf<String?>(null) }

    // Initial load
    LaunchedEffect(Unit) {
        val currentUser = AuthManager.getCurrentUser()
        setUser(currentUser)
        setStartDestination(
            if (currentUser.type == "registered" && !currentUser.token.isNullOrBlank()) "home"
            else "start"
        )
    }

    // When ready, launch AppNavHost with fresh user
    if (user != null && startDestination != null) {
        AppNavHost(
            startDestination = startDestination,
            user = user,
            onForceRestart = {
                // Refresh user and re-launch from root
                val refreshedUser = AuthManager.getCurrentUser()
                setUser(refreshedUser)
                setStartDestination(
                    if (refreshedUser.type == "registered" && !refreshedUser.token.isNullOrBlank()) "home"
                    else "start"
                )
                restartApp()
            }
        )
    }
}
