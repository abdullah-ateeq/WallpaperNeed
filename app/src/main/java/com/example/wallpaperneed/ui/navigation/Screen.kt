package com.example.wallpaperneed.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Splash : Screen("splash", "Splash", Icons.Default.Home)
    data object Home : Screen("home", "WallpaperNeed", Icons.Default.Calculate)
    data object CuttingPlan : Screen("cutting_plan", "Cutting Plan", Icons.Default.ContentCut)
    data object MultipleRooms : Screen("multiple_rooms", "Multiple Rooms", Icons.Default.MeetingRoom)
    data object History : Screen("history", "History", Icons.Default.History)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object About : Screen("about", "About", Icons.Default.Info)

    companion object {
        // Items displayed in the bottom navigation bar or drawer
        val bottomNavItems = listOf(
            Home,
            CuttingPlan,
            MultipleRooms,
            History,
            Settings
        )
    }
}
