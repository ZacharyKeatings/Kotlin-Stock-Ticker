package com.example.stockticker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.stockticker.auth.AuthManager
import com.example.stockticker.network.SocketManager
import com.example.stockticker.ui.screens.*
import com.example.stockticker.viewmodel.GameViewModel

@Composable
fun AppNavHost(startDestination: String = "start") {
    val navController = rememberNavController()
    val socket        = remember { SocketManager.getSocket() }
    val gameVm: GameViewModel = viewModel()

    // Pull current user just once, here:
    val me = AuthManager.getCurrentUser()
    val username = me.username            // e.g. "Guest1234" or registered name
    val token    = me.token.orEmpty()     // empty string if guest

    NavHost(navController = navController, startDestination = startDestination) {

        // ─── Start / Auth ───────────────────────────────────────────────────
        composable("start") {
            StartScreen(
                onPlayAsGuest = { navController.navigate("home") },
                onLoginClick   = { navController.navigate("login") },
                onRegisterClick= { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { user, tok ->
                    // Save the new token, then go home
                    AuthManager.saveToken(tok)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { user, tok ->
                    AuthManager.saveToken(tok)
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Home Screen ──────────────────────────────────────────────────────
        composable("home") {
            HomeScreen(
                username     = username,
                token        = token,
                onCreateGame = { navController.navigate("createGame") },
                onJoinGame   = { navController.navigate("joinGame") },
                onFindGame   = { navController.navigate("gameList") },
                onRejoinGame = { gameId ->
                    navController.navigate("inGame/$gameId")
                },
                onProfile    = { navController.navigate("profile") },
                onSettings   = { navController.navigate("settings") }
            )
        }

        // ─── Create Game ─────────────────────────────────────────────────────
        composable("createGame") {
            CreateGameScreen(
                username = username,
                token    = token,
                onBack   = { navController.popBackStack() },
                onCreate = { rounds, maxPlayers, aiCount, isPublic ->
                    gameVm.clearGameState()
                    gameVm.createGame(
                        rounds     = rounds,
                        maxPlayers = maxPlayers,
                        aiCount    = aiCount,
                        isPublic   = isPublic,
                        username   = if (me.type == "guest") null else username,
                        token      = if (me.type == "guest") null else token,
                        onSuccess  = { newGameId ->
                            navController.navigate("lobby/$newGameId")
                        },
                        onError    = { /* show error */ }
                    )
                }
            )
        }

        // ─── Join Game by ID ─────────────────────────────────────────────────
        composable("joinGame") {
            JoinGameScreen(
                username      = username,
                token         = token,
                onBack        = { navController.popBackStack() },
                onJoinSuccess = { gameId ->
                    gameVm.clearGameState()
                    gameVm.joinGame(
                        gameId   = gameId,
                        username = if (me.type == "guest") null else username,
                        token    = if (me.type == "guest") null else token,
                        onSuccess = {
                            navController.navigate("lobby/$gameId")
                        },
                        onError = { /* show error */ }
                    )
                },
                onError       = { /* show error */ }
            )
        }

        // ─── Public Game List ────────────────────────────────────────────────
        composable("gameList") {
            GameListScreen(
                username = username,
                token    = token,
                onJoin   = { gameId ->
                    gameVm.clearGameState()
                    gameVm.joinGame(
                        gameId   = gameId,
                        username = if (me.type == "guest") null else username,
                        token    = if (me.type == "guest") null else token,
                        onSuccess = {
                            navController.navigate("lobby/$gameId") {
                                popUpTo("gameList") { inclusive = true }
                            }
                        },
                        onError = { /* show error */ }
                    )
                },
                onBack   = { navController.popBackStack() }
            )
        }

        // ─── Lobby ───────────────────────────────────────────────────────────
        composable("lobby/{gameId}") { backStack ->
            val gameId = backStack.arguments!!.getString("gameId")!!
            LobbyScreen(
                navController = navController,
                gameId        = gameId,
                username      = if (me.type == "guest") null else username,
                token         = if (me.type == "guest") null else token,
                gameVm        = gameVm
            )
        }

        // ─── In‐Game ──────────────────────────────────────────────────────────
        composable("inGame/{gameId}") { backStack ->
            val gameId = backStack.arguments!!.getString("gameId")!!
            InGameScreen(
                gameId       = gameId,
                socketId     = socket.id(),
                username     = if (me.type == "guest") null else username,
                token        = if (me.type == "guest") null else token,
                gameVm       = gameVm,
                onToast      = { /* show toast */ },
                onGameComplete = {
                    navController.navigate("gameOver/$gameId") {
                        popUpTo("inGame/$gameId") { inclusive = true }
                    }
                },
                onReturnHome = {
                    navController.navigate("home") {
                        popUpTo("inGame/$gameId") { inclusive = true }
                    }
                }
            )
        }

        // ─── Game Over ────────────────────────────────────────────────────────
        composable("gameOver/{gameId}") { backStack ->
            val gameId = backStack.arguments!!.getString("gameId")!!
            GameOverScreen(
                gameVm       = gameVm,
                onReturnHome = {
                    navController.navigate("home") {
                        popUpTo("gameOver/$gameId") { inclusive = true }
                    }
                }
            )
        }

        // ─── Profile & Settings ───────────────────────────────────────────────
        composable("profile") {
            ProfileScreen(
                onBack   = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                username = username,
                token    = token,
                onBack   = { navController.popBackStack() }
            )
        }
    }
}
