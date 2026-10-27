package com.example.wallpaperneed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.wallpaperneed.ui.navigation.WallpaperNavGraph
import com.example.wallpaperneed.ui.theme.WallpaperNeedTheme
import com.example.wallpaperneed.viewmodel.WallpaperViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val wallpaperViewModel: WallpaperViewModel = viewModel()
            val settings by wallpaperViewModel.settings.collectAsState()

            WallpaperNeedTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    WallpaperNavGraph(
                        navController = navController,
                        viewModel = wallpaperViewModel
                    )
                }
            }
        }
    }
}
