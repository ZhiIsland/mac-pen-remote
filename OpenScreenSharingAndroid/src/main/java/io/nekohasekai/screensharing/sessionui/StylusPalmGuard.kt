package io.nekohasekai.screensharing.sessionui

/** Keeps a short bridge between HOVER_EXIT and pen-down, without holding fingers indefinitely. */
internal class StylusPalmGuard {
    private var hovering = false
    private val contacts = HashSet<Int>()
    private var lastPenTime: Long? = null

    fun hover(near: Boolean, time: Long) {
        hovering = near
        lastPenTime = time
    }

    fun contact(id: Int, down: Boolean, time: Long) {
        if (down) contacts.add(id) else contacts.remove(id)
        lastPenTime = time
    }

    fun rejectsFinger(time: Long): Boolean = hovering || contacts.isNotEmpty() ||
        lastPenTime?.let { time - it in 0..150 } == true

    fun reset() {
        hovering = false
        contacts.clear()
        lastPenTime = null
    }
}
