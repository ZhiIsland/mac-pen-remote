package io.nekohasekai.screensharing.sessionui

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot

/** Converts local pen sensors into ordinary scroll deltas; no server-side pen support is needed. */
internal class StylusScrollDynamics {
    private var previousTime: Long? = null
    private var smoothedGain = 1f
    private var travelX = 0f
    private var travelY = 0f
    private var axis = 0

    fun reset() {
        previousTime = null
        smoothedGain = 1f
        travelX = 0f
        travelY = 0f
        axis = 0
    }

    fun apply(
        x: Float,
        y: Float,
        time: Long,
        density: Float,
        pressure: Float?,
        acceleratesHover: Boolean,
        locksAxis: Boolean,
    ): StylusButtonGesture.Delta {
        if (!x.isFinite() || !y.isFinite()) return StylusButtonGesture.Delta(0f, 0f)
        val elapsed = previousTime?.let { (time - it).coerceIn(1, 250) } ?: 16L
        previousTime = time
        val normalizedPressure = pressure?.takeIf { it.isFinite() }?.coerceIn(0f, 1f)
        val speed = hypot(x, y) / density.coerceAtLeast(0.1f) * 1000f / elapsed
        val target = when {
            normalizedPressure != null -> 0.45f + 1.8f * normalizedPressure
            acceleratesHover -> 1f + ((speed - 120f) / 700f).coerceIn(0f, 1f) * 1.5f
            else -> 1f
        }
        val alpha = 1f - exp(-elapsed / 50f)
        smoothedGain += (target - smoothedGain) * alpha
        travelX += x
        travelY += y
        if (locksAxis && axis == 0 && hypot(travelX, travelY) >= 8f * density) {
            if (abs(travelY) > abs(travelX) * 1.6f) axis = 1
            if (abs(travelX) > abs(travelY) * 1.6f) axis = 2
        }
        return StylusButtonGesture.Delta(
            if (locksAxis && axis == 1) 0f else x * smoothedGain,
            if (locksAxis && axis == 2) 0f else y * smoothedGain,
        )
    }

    companion object {
        fun normalizePressure(value: Float, minimum: Float?, maximum: Float?): Float? {
            if (!value.isFinite()) return null
            val low = minimum?.takeIf { it.isFinite() } ?: 0f
            val high = maximum?.takeIf { it.isFinite() } ?: 1f
            if (high <= low) return null
            return ((value - low) / (high - low)).coerceIn(0f, 1f)
        }
    }
}
