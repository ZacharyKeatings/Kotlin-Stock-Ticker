package com.example.stockticker.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.example.stockticker.auth.AuthManager
//
//@Composable
//fun GamesScreen(
//    navController: NavController,
//    username: String = "GuestUser",
//    token: String? = null
//) {
//    var selectedTabIndex by remember { mutableIntStateOf(0) }
//
//    val tabs = listOf("Create", "Join", "Game List")
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        TabRow(
//            selectedTabIndex = selectedTabIndex,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            tabs.forEachIndexed { index, title ->
//                Tab(
//                    selected = selectedTabIndex == index,
//                    onClick = { selectedTabIndex = index },
//                    text = { Text(title) }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        when (selectedTabIndex) {
//            0 -> CreateGameTab(
//                username = username,
//                token = token,
//                onGameCreated = { gameId ->
//                    navController.navigate("game/$gameId")
//                }
//            )
//            1 -> JoinGameTab()
//            2 -> GameListTab()
//        }
//    }
//}
