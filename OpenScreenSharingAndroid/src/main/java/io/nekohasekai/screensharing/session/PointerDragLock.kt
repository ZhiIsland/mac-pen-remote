package io.nekohasekai.screensharing.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** A latched primary button survives ordinary contacts, but never a session/input reset. */
internal class PointerDragLock {
    var active: Boolean by mutableStateOf(false)
        private set

    fun set(active: Boolean) {
        this.active = active
    }

    fun buttons(requested: PointerButtons): PointerButtons = if (active) requested + PointerButtons.Primary else requested

    fun clear() {
        active = false
    }
}
