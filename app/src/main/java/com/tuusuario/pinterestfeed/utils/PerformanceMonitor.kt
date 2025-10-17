package com.tuusuario.pinterestfeed.utils

import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.metrics.performance.FrameData
import androidx.metrics.performance.JankStats
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Monitor de rendimiento con JankStats real
 * Mide frame timing y detecta jank durante el scroll
 */
class PerformanceMonitor(private val view: View) {

    private var jankStats: JankStats? = null
    private val tag = "PerformanceMonitor"

    // Estadísticas acumuladas
    private var totalFrames = 0
    private var jankFrames = 0
    private val jankThresholdNanos = TimeUnit.MILLISECONDS.toNanos(16) // 16ms = 60fps

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun start() {
        jankStats = JankStats.createAndTrack(
            view.window,
            jankStatsListener
        )

        totalFrames = 0
        jankFrames = 0

        Log.d(tag, "═══════════════════════════════════════")
        Log.d(tag, "📊 JankStats Monitor Started")
        Log.d(tag, "Time: ${dateFormat.format(Date())}")
        Log.d(tag, "═══════════════════════════════════════")
    }

    fun stop() {
        jankStats?.isTrackingEnabled = false

        val jankRate = if (totalFrames > 0) {
            (jankFrames.toFloat() / totalFrames) * 100
        } else 0f

        Log.d(tag, "")
        Log.d(tag, "═══════════════════════════════════════")
        Log.d(tag, "📈 PERFORMANCE SUMMARY")
        Log.d(tag, "═══════════════════════════════════════")
        Log.d(tag, "Total Frames: $totalFrames")
        Log.d(tag, "Jank Frames: $jankFrames")
        Log.d(tag, "Jank Rate: %.2f%%".format(jankRate))
        Log.d(tag, "Target: ≤ 5.0%")

        if (jankRate <= 5.0f) {
            Log.d(tag, "✅ PASSED - Excellent performance!")
        } else if (jankRate <= 12.0f) {
            Log.d(tag, "⚠️  WARNING - Performance needs improvement")
        } else {
            Log.d(tag, "❌ FAILED - Poor performance detected")
        }

        Log.d(tag, "═══════════════════════════════════════")
        Log.d(tag, "")

        jankStats = null
    }

    private val jankStatsListener = JankStats.OnFrameListener { frameData ->
        totalFrames++

        val frameDurationNanos = frameData.frameDurationUiNanos
        val frameDurationMs = TimeUnit.NANOSECONDS.toMillis(frameDurationNanos)

        if (frameData.isJank) {
            jankFrames++

            Log.w(
                tag,
                "🔴 JANK #$jankFrames | Frame: $totalFrames | " +
                        "Duration: ${frameDurationMs}ms (${frameDurationNanos}ns) | " +
                        "States: ${frameData.states.joinToString()}"
            )
        } else if (totalFrames % 60 == 0) {
            // Log cada 60 frames (~1 segundo a 60fps)
            val currentJankRate = (jankFrames.toFloat() / totalFrames) * 100
            Log.d(
                tag,
                "✅ Frame $totalFrames | Duration: ${frameDurationMs}ms | " +
                        "Jank Rate: %.2f%%".format(currentJankRate)
            )
        }
    }
}

/**
 * Composable que maneja el ciclo de vida del monitor
 */
@Composable
fun rememberJankStatsMonitor(): PerformanceMonitor? {
    val view = LocalView.current

    val monitor = remember(view) {
        if (!view.isInEditMode) {
            PerformanceMonitor(view)
        } else null
    }

    DisposableEffect(monitor) {
        monitor?.start()
        onDispose {
            monitor?.stop()
        }
    }

    return monitor
}

/**
 * Extensión para verificar si un frame es jank
 */
private val FrameData.isJank: Boolean
    get() {
        val sixteenMs = TimeUnit.MILLISECONDS.toNanos(16)
        return frameDurationUiNanos > sixteenMs
    }