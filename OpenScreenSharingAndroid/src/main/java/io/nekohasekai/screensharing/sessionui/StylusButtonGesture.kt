package io.nekohasekai.screensharing.sessionui

import kotlin.math.hypot

/** Delays the right click until pen-up so a scroll never opens a context menu. */
internal class StylusButtonGesture(private val slop: Float) {
    data class Delta(val x: Float, val y: Float)

    private var pointer: Int? = null
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var scrolling = false

    fun owns(id: Int): Boolean = pointer == id

    fun begin(id: Int, x: Float, y: Float, alreadyScrolling: Boolean = false) {
        pointer = id
        startX = x
        startY = y
        lastX = x
        lastY = y
        scrolling = alreadyScrolling
    }

    fun move(id: Int, x: Float, y: Float): Delta? {
        if (!owns(id)) return null
        if (!scrolling && hypot(x - startX, y - startY) <= slop) return null
        scrolling = true
        val delta = Delta(x - lastX, y - lastY)
        lastX = x
        lastY = y
        return delta.takeIf { it.x != 0f || it.y != 0f }
    }

    /** True only for a tap; cancellation, scrolling and other pointers never click. */
    fun finish(id: Int, cancelled: Boolean): Boolean {
        if (!owns(id)) return false
        val click = !cancelled && !scrolling
        cancel()
        return click
    }

    fun cancel() {
        pointer = null
        scrolling = false
    }
}
