package com.tuusuario.pinterestfeed.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Implementación no-op del monitor de rendimiento.
 * No requiere androidx.metrics.performance.
 */
@Composable
fun rememberJankStatsMonitor(
    enabled: Boolean = false, // ignorado en no-op
    onFrame: (Any) -> Unit = { _ -> }
): Any? {
    val monitor = remember { Any() }
    DisposableEffect(Unit) {
        onDispose { /* no-op */ }
    }
    return monitor
}
