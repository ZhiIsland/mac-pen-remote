package io.nekohasekai.screensharing.sessionui

/** Accepts both hover button-state changes and generic button events without duplicating clicks. */
internal class StylusHoverGesture(slop: Float) {
    data class Change(
        val began: Boolean = false,
        val delta: StylusButtonGesture.Delta? = null,
        val ended: Boolean = false,
        val cancelled: Boolean = false,
        val rightClick: Boolean = false,
    )

    private val gesture = StylusButtonGesture(slop)
    private var pointer: Int? = null
    private var needsRelease = false
    private var scrolledSincePress = false
    val isActive: Boolean get() = pointer != null

    fun update(id: Int, x: Float, y: Float, pressed: Boolean, eligible: Boolean): Change {
        val active = pointer
        if (!eligible || (active != null && active != id)) {
            val change = blockUntilRelease()
            if (!pressed) {
                needsRelease = false
                scrolledSincePress = false
            }
            return change
        }
        if (!pressed) {
            needsRelease = false
            scrolledSincePress = false
            if (active == null) return Change()
            // Button release can carry the final movement, just like pen-up.
            val delta = gesture.move(id, x, y)
            val click = gesture.finish(id, cancelled = false)
            pointer = null
            return Change(delta = delta, ended = true, rightClick = click)
        }
        if (needsRelease) return Change()
        if (active == null) {
            pointer = id
            scrolledSincePress = false
            gesture.begin(id, x, y)
            return Change(began = true)
        }
        val delta = gesture.move(id, x, y)
        if (delta != null) scrolledSincePress = true
        return Change(delta = delta)
    }

    /** Continue a hover scroll on contact without turning the same button press into a right click. */
    fun beginContact(pressed: Boolean): Boolean {
        val continuesScroll = pressed && scrolledSincePress
        blockUntilRelease()
        scrolledSincePress = false
        return continuesScroll
    }

    /** Hover exit, pen contact and focus loss must never synthesize a click. */
    fun blockUntilRelease(): Change {
        val ended = isActive
        pointer = null
        gesture.cancel()
        needsRelease = true
        return Change(ended = ended, cancelled = true)
    }
}
