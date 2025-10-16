package com.tuusuario.pinterestfeed.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.metrics.performance.FrameData
import androidx.metrics.performance.JankStats

/**
 * Monitor de rendimiento usando JankStats
 * Mide jank rate y frame times
 */
class PerformanceMonitor {
    private val tag = "PerformanceMonitor"
    private val frameData = mutableListOf<FrameData>()
    private var startTime = 0L

    fun onFrame(frameData: FrameData) {
        this.frameData.add(frameData)

        // Log cada 100 frames
        if (this.frameData.size % 100 == 0) {
            logStats()
        }
    }

    fun start() {
        startTime = System.currentTimeMillis()
        frameData.clear()
        Log.d(tag, "Performance monitoring started")
    }

    fun stop() {
        val duration = System.currentTimeMillis() - startTime
        Log.d(tag, "Performance monitoring stopped. Duration: ${duration}ms")
        logStats()
    }

    private fun logStats() {
        if (frameData.isEmpty()) return

        val jankFrames = frameData.count { it.isJank }
        val totalFrames = frameData.size
        val jankRate = (jankFrames.toFloat() / totalFrames) * 100

        val frameTimes = frameData.map { it.frameDurationUiNanos / 1_000_000.0 }
        val avgFrameTime = frameTimes.average()
        val maxFrameTime = frameTimes.maxOrNull() ?: 0.0

        Log.d(tag, """
            Performance Stats:
            - Total Frames: $totalFrames
            - Jank Frames: $jankFrames
            - Jank Rate: ${"%.2f".format(jankRate)}%
            - Avg Frame Time: ${"%.2f".format(avgFrameTime)}ms
            - Max Frame Time: ${"%.2f".format(maxFrameTime)}ms
            - Target: ≤ 5% jank rate
        """.trimIndent())
    }

    fun getJankRate(): Float {
        if (frameData.isEmpty()) return 0f
        val jankFrames = frameData.count { it.isJank }
        return (jankFrames.toFloat() / frameData.size) * 100
    }
}

/**
 * Composable para integrar JankStats en la UI
 */
@Composable
fun rememberJankStatsMonitor(): PerformanceMonitor {
    val view = LocalView.current
    val monitor = remember { PerformanceMonitor() }

    DisposableEffect(view) {
        val jankStats = JankStats.createAndTrack(
            view.parent as android.view.Window,
            monitor::onFrame
        )

        monitor.start()

        onDispose {
            monitor.stop()
            jankStats.isTrackingEnabled = false
        }
    }

    return monitor
}