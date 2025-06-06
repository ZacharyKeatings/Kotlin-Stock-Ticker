package com.example.stockticker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.stockticker.auth.AuthManager
import com.example.stockticker.auth.UserIdentity
import com.example.stockticker.ui.navigation.AppNavHost

/**
 * Root Composable for the app.
 *
 * On first composition, it asks AuthManager.getCurrentUser() to figure out:
 *  • If the stored token is valid (a registered user) → startDestination = "home/{username}/{token}"
 *  • Otherwise (fallback to a guest) → startDestination = "start"
 *
 * We use a remembered mutableState to hold that route, so Compose will only compute it once.
 */
@Composable
fun StockTickerApp() {
    // Holds the final route that NavHost should start with. Null until we check auth state.
    val (startDestination, setStartDestination) = remember { mutableStateOf<String?>(null) }

    // When this Composable first enters composition, determine which route to use:
    LaunchedEffect(Unit) {
        val user: UserIdentity = AuthManager.getCurrentUser()

        if (user.type == "registered" && !user.token.isNullOrBlank()) {
            // Registered user found → navigate straight to home/{username}/{token}
            setStartDestination("home/${user.username}/${user.token}")
        } else {
            // Either token is missing/invalid or explicitly a guest → go to “start”
            setStartDestination("start")
        }
    }

    // Until we have computed startDestination, render nothing. Once non‐null, launch the NavHost.
    startDestination?.let { initialRoute ->
        AppNavHost(startDestination = initialRoute)
    }
}
