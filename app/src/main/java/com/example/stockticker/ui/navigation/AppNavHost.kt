// AppNavHost.kt
package com.example.stockticker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.stockticker.network.SocketManager
import com.example.stockticker.ui.screens.*
import com.example.stockticker.viewmodel.GameViewModel

@Composable
fun AppNavHost(startDestination: String = "start") {
    val navController = rememberNavController()

    // Create or retrieve the singleton Socket.IO client once
    val socket = remember { SocketManager.getSocket() }

    // Create ONE ViewModel instance here, shared by all routes.
    val gameVm: GameViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        // ─── Start Screen ─────────────────────────────────────────────────────
        composable("start") {
            StartScreen(
                onPlayAsGuest = {
                    navController.navigate("home/guest/") // token empty
                },
                onLoginClick = {
                    navController.navigate("login")
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        // ─── Login Screen ─────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                onLoginSuccess = { username, token ->
                    navController.navigate("home/$username/$token") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ─── Register Screen ──────────────────────────────────────────────────
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { username, token ->
                    navController.navigate("home/$username/$token") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ─── Home Screen (Logged‐In) ───────────────────────────────────────────
        composable(
            route = "home/{username}/{token}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("token")    { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments!!.getString("username")!!
            val token    = backStackEntry.arguments!!.getString("token")!!

            HomeScreen(
                username    = username,
                token       = token,
                onCreateGame = {
                    navController.navigate("createGame/$username/$token")
                },
                onJoinGame   = {
                    navController.navigate("joinGame/$username/$token")
                },
                onFindGame   = {
                    navController.navigate("gameList/$username/$token")
                },
                onRejoinGame = { gameId ->
                    navController.navigate("inGame/$gameId/$username/$token")
                },
                onProfile    = {
                    navController.navigate("profile/$username/$token")
                },
                onSettings   = {
                    navController.navigate("settings/$username/$token")
                }
            )
        }

        // ─── Home Screen (Guest) ──────────────────────────────────────────────
        composable("home/guest/") {
            HomeScreen(
                username    = "guest",
                token       = "",
                onCreateGame = {
                    navController.navigate("createGame/guest/")
                },
                onJoinGame   = {
                    navController.navigate("joinGame/guest/")
                },
                onFindGame   = {
                    navController.navigate("gameList/guest/")
                },
                onRejoinGame = null,   // no rejoin for guest
                onProfile    = null,   // no profile for guest
                onSettings   = null    // no settings for guest
            )
        }

        // ─── Create Game Screen ──────────────────────────────────────────────
        composable(
            route = "createGame/{usernameOrGuest}/{tokenOrEmpty}",
            arguments = listOf(
                navArgument("usernameOrGuest") { type = NavType.StringType },
                navArgument("tokenOrEmpty")   { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val usernameOrGuest = backStackEntry.arguments!!.getString("usernameOrGuest")!!
            val tokenOrEmpty    = backStackEntry.arguments!!.getString("tokenOrEmpty")!!

            CreateGameScreen(
                username = usernameOrGuest,
                token    = tokenOrEmpty,
                onBack = {
                    navController.popBackStack()
                },
                onCreate = { rounds, maxPlayers, aiCount, isPublic ->
                    gameVm.clearGameState()
                    // Now that 'vm' is in scope, call its createGame(...) method:
                    gameVm.createGame(
                        rounds      = rounds,
                        maxPlayers  = maxPlayers,
                        aiCount     = aiCount,
                        isPublic    = isPublic,
                        username    = if (usernameOrGuest == "guest") null else usernameOrGuest,
                        token       = if (usernameOrGuest == "guest") null else tokenOrEmpty,
                        onSuccess   = { newGameId ->
                            navController.navigate(
                                "lobby/$newGameId/$usernameOrGuest/$tokenOrEmpty"
                            )
                        },
                        onError     = { msg ->
                            // TODO: show a toast or dialog with the error
                        }
                    )
                }
            )
        }

        // ─── Game List (Public Games) ────────────────────────────────────────
        composable(
            route = "gameList/{usernameOrGuest}/{tokenOrEmpty}",
            arguments = listOf(
                navArgument("usernameOrGuest") { type = NavType.StringType },
                navArgument("tokenOrEmpty")   { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val usernameOrGuest = backStackEntry.arguments!!.getString("usernameOrGuest")!!
            val tokenOrEmpty    = backStackEntry.arguments!!.getString("tokenOrEmpty")!!

            GameListScreen(
                username = usernameOrGuest,
                token    = tokenOrEmpty,

                onJoin   = { gameId ->
                    // 1) Optionally reset any stale state
                    gameVm.clearGameState()

                    // 2) Emit the join event
                    gameVm.joinGame(
                        gameId = gameId,
                        username = if (usernameOrGuest == "guest") null else usernameOrGuest,
                        token    = if (usernameOrGuest == "guest") null else tokenOrEmpty,

                        onSuccess = {
                            // 3) Only once the server says “you’re in”, navigate to the lobby
                            navController.navigate("lobby/$gameId/$usernameOrGuest/$tokenOrEmpty") {
                                popUpTo("gameList/$usernameOrGuest/$tokenOrEmpty") { inclusive = true }
                            }
                        },

                        onError = { err ->
                            // TODO: surface this to the user via a Snackbar or dialog
                            println("Failed to join game $gameId: $err")
                        }
                    )
                },

                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ─── Join Game by ID ──────────────────────────────────────────────────
        composable(
            route = "joinGame/{usernameOrGuest}/{tokenOrEmpty}",
            arguments = listOf(
                navArgument("usernameOrGuest") { type = NavType.StringType },
                navArgument("tokenOrEmpty")   { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val usernameOrGuest = backStackEntry.arguments!!.getString("usernameOrGuest")!!
            val tokenOrEmpty    = backStackEntry.arguments!!.getString("tokenOrEmpty")!!

            JoinGameScreen(
                username      = usernameOrGuest,
                token         = tokenOrEmpty,
                onBack        = {
                    navController.popBackStack()
                },
                onJoinSuccess = { gameId ->
                    navController.navigate("lobby/$gameId/$usernameOrGuest/$tokenOrEmpty")
                },
                onError       = { msg ->
                    // TODO: show a Snack bar or dialog with the error
                }
            )
        }

        // ─── Lobby Screen ─────────────────────────────────────────────────────
        composable(
            route = "lobby/{gameId}/{usernameOrGuest}/{tokenOrEmpty}",
            arguments = listOf(
                navArgument("gameId")           { type = NavType.StringType },
                navArgument("usernameOrGuest")  { type = NavType.StringType },
                navArgument("tokenOrEmpty")     { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameId          = backStackEntry.arguments!!.getString("gameId")!!
            val usernameOrGuest = backStackEntry.arguments!!.getString("usernameOrGuest")!!
            val tokenOrEmpty    = backStackEntry.arguments!!.getString("tokenOrEmpty")!!

            LobbyScreen(
                navController = navController,
                gameId        = gameId,
                username      = if (usernameOrGuest == "guest") null else usernameOrGuest,
                token         = if (usernameOrGuest == "guest") null else tokenOrEmpty,
                gameVm        = gameVm
            )
        }

        // ─── In‐Game Screen ───────────────────────────────────────────────────
        composable(
            route = "inGame/{gameId}/{usernameOrGuest}/{tokenOrEmpty}",
            arguments = listOf(
                navArgument("gameId")           { type = NavType.StringType },
                navArgument("usernameOrGuest")  { type = NavType.StringType },
                navArgument("tokenOrEmpty")     { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameId          = backStackEntry.arguments!!.getString("gameId")!!
            val usernameOrGuest = backStackEntry.arguments!!.getString("usernameOrGuest")!!
            val tokenOrEmpty    = backStackEntry.arguments!!.getString("tokenOrEmpty")!!

            InGameScreen(
                gameId       = gameId,
                socketId     = socket.id(),
                username     = if (usernameOrGuest == "guest") null else usernameOrGuest,
                token        = if (usernameOrGuest == "guest") null else tokenOrEmpty,
                gameVm       = gameVm,
                onToast      = { msg ->
                    // show a Snackbar or dialog if desired
                },
                onGameComplete = {
                    // Navigate to your GameOverScreen route
                    if (usernameOrGuest == "guest") {
                        navController.navigate("gameOver/$gameId/guest/") {
                            popUpTo("inGame/$gameId/$usernameOrGuest/$tokenOrEmpty") { inclusive = true }
                        }
                    } else {
                        navController.navigate("gameOver/$gameId/$usernameOrGuest/$tokenOrEmpty") {
                            popUpTo("inGame/$gameId/$usernameOrGuest/$tokenOrEmpty") { inclusive = true }
                        }
                    }
                },
                onReturnHome = {
                    if (usernameOrGuest == "guest") {
                        navController.navigate("home/guest/") {
                            popUpTo("inGame/$gameId/$usernameOrGuest/$tokenOrEmpty") {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate("home/$usernameOrGuest/$tokenOrEmpty") {
                            popUpTo("inGame/$gameId/$usernameOrGuest/$tokenOrEmpty") {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        // ─── Game Over Screen ──────────────────────────────────────────────────
        composable(
            route = "gameOver/{gameId}/{usernameOrGuest}/{tokenOrEmpty}",
            arguments = listOf(
                navArgument("gameId")           { type = NavType.StringType },
                navArgument("usernameOrGuest")  { type = NavType.StringType },
                navArgument("tokenOrEmpty")     { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameId          = backStackEntry.arguments!!.getString("gameId")!!
            val usernameOrGuest = backStackEntry.arguments!!.getString("usernameOrGuest")!!
            val tokenOrEmpty    = backStackEntry.arguments!!.getString("tokenOrEmpty")!!

            GameOverScreen(
                gameVm = gameVm,
                onReturnHome = {
                    if (usernameOrGuest == "guest") {
                        navController.navigate("home/guest/") {
                            popUpTo("gameOver/$gameId/$usernameOrGuest/$tokenOrEmpty") {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate("home/$usernameOrGuest/$tokenOrEmpty") {
                            popUpTo("gameOver/$gameId/$usernameOrGuest/$tokenOrEmpty") {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        // ─── Profile Screen ───────────────────────────────────────────────────
        composable(
            route = "profile/{username}/{token}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("token")    { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments!!.getString("username")!!
            val token    = backStackEntry.arguments!!.getString("token")!!

            ProfileScreen(
                username = username,
                token    = token,
                onBack   = {
                    navController.popBackStack()
                }
            )
        }

        // ─── Settings Screen ──────────────────────────────────────────────────
        composable(
            route = "settings/{username}/{token}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("token")    { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments!!.getString("username")!!
            val token    = backStackEntry.arguments!!.getString("token")!!

            SettingsScreen(
                username = username,
                token    = token,
                onBack   = {
                    navController.popBackStack()
                }
            )
        }
    }
}
