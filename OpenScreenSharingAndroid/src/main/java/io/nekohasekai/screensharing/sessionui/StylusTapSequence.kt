package io.nekohasekai.screensharing.sessionui

import kotlin.math.hypot

/** Optional hover double-click arbitration. Disabled double-clicks have no added single-click delay. */
internal class StylusTapSequence(private val interval: Long, private val slop: Float) {
    data class Tap(val x: Float, val y: Float, val time: Long)
    data class Result(val single: Tap? = null, val double: Boolean = false, val waits: Boolean = false)

    private var pending: Tap? = null

    fun tap(x: Float, y: Float, time: Long, doubleEnabled: Boolean): Result {
        val next = Tap(x, y, time)
        val previous = pending
        pending = null
        if (!doubleEnabled) return Result(single = next)
        if (previous != null && time - previous.time in 0..interval && hypot(x - previous.x, y - previous.y) <= slop) {
            return Result(double = true)
        }
        pending = next
        return Result(single = previous, waits = true)
    }

    fun timeout(time: Long): Tap? {
        val tap = pending ?: return null
        if (time - tap.time < interval) return null
        pending = null
        return tap
    }

    fun cancel() {
        pending = null
    }
}
