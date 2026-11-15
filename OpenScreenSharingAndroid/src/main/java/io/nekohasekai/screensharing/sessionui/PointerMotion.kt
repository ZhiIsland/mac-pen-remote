package io.nekohasekai.screensharing.sessionui

import kotlin.math.min

internal object PointerMotion {
    fun gain(speed: Float, density: Float, preference: Float, accelerates: Boolean): Float {
        val multiplier = preference.takeIf { it.isFinite() }?.coerceIn(0.25f, 3f) ?: 1f
        if (!accelerates) return multiplier
        val normalizedSpeed = (speed.takeIf { it.isFinite() } ?: 0f).coerceAtLeast(0f)
        val displayDensity = density.takeIf { it.isFinite() && it > 0f } ?: 1f
        return multiplier * (1.2f + min(normalizedSpeed / displayDensity / 1200f, 1.8f))
    }
}
