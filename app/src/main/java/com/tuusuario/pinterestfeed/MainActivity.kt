package com.tuusuario.pinterestfeed

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import coil.Coil
import coil.ImageLoader
import coil.util.DebugLogger
import com.tuusuario.pinterestfeed.navigation.AppNavigation
import com.tuusuario.pinterestfeed.ui.theme.PinterestFeedTheme
import com.tuusuario.pinterestfeed.utils.rememberJankStatsMonitor

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configurar Coil con logger para evidenciar cache hits
        setupCoilLogger()

        Log.d(tag, "═══════════════════════════════════════")
        Log.d(tag, "🚀 Pinterest Feed App Started")
        Log.d(tag, "═══════════════════════════════════════")

        setContent {
            PinterestFeedTheme {
                // Monitor de performance integrado
                rememberJankStatsMonitor()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    /**
     * Configura Coil con logger para debug
     * Permite ver cache hits/misses en Logcat
     */
    private fun setupCoilLogger() {
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .respectCacheHeaders(false) // Cache agresivo
                .logger(DebugLogger(Log.DEBUG))
                .build()
        )

        Log.d(tag, "✅ Coil ImageLoader configured with cache logging")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "═══════════════════════════════════════")
        Log.d(tag, "👋 App Destroyed")
        Log.d(tag, "═══════════════════════════════════════")
    }
}