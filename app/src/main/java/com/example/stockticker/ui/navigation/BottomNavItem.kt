package com.example.stockticker.ui.navigation
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Games
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.ui.graphics.vector.ImageVector
//
//sealed class BottomNavItem(
//    val route: String,
//    val title: String,
//    val icon: ImageVector
//) {
//    object Games : BottomNavItem("games", "Games", Icons.Filled.Games)
//    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
//    object Settings : BottomNavItem("settings", "Settings", Icons.Filled.Settings)
//
//    companion object {
//        val allItems = listOf(Games, Profile, Settings)
//    }
//}