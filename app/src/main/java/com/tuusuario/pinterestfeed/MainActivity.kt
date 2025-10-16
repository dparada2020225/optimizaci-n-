package com.tuusuario.pinterestfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tuusuario.pinterestfeed.navigation.AppNavigation
import com.tuusuario.pinterestfeed.ui.theme.PinterestFeedTheme
import com.tuusuario.pinterestfeed.utils.rememberJankStatsMonitor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PinterestFeedTheme {
                // Monitor de performance
                val monitor = rememberJankStatsMonitor()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}