package com.example.stockticker.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun JoinGameTab() {
//    var gameId by remember { mutableStateOf("") }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("Join Existing Game")
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = gameId,
//            onValueChange = { gameId = it },
//            label = { Text("Game ID") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = {
//            // TODO: Hook up to join game logic
//        }) {
//            Text("Join Game")
//        }
//    }
//}