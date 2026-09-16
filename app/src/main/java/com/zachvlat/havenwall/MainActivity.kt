package com.zachvlat.havenwall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zachvlat.havenwall.ui.screens.WallpaperScreen
import com.zachvlat.havenwall.ui.theme.HavenwallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HavenwallTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val wallpaperViewModel: WallpaperViewModel = viewModel()
                    WallpaperScreen(
                        viewModel = wallpaperViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
