package io.nekohasekai.screensharing.sessionui

import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Local, in-memory readings only. Advertised axes are not assumed to contain usable samples. */
internal object PenInputTelemetry {
    data class Reading(
        val device: String,
        val pressure: Float?,
        val tilt: Float?,
        val orientation: Float?,
        val distance: Float?,
        val hovering: Boolean,
        val sideButton: Boolean,
    )

    var reading: Reading? by mutableStateOf(null)
        private set
    private var lastTime = 0L

    fun record(event: MotionEvent, index: Int = 0, force: Boolean = false) {
        if (event.getToolType(index) != MotionEvent.TOOL_TYPE_STYLUS && event.getToolType(index) != MotionEvent.TOOL_TYPE_ERASER) return
        if (!force && event.eventTime - lastTime in 0..80) return
        lastTime = event.eventTime
        fun axis(axis: Int): Float? = event.device?.getMotionRange(axis, event.source)
            ?.let { event.getAxisValue(axis, index).takeIf { value -> value.isFinite() } }
        val range = event.device?.getMotionRange(MotionEvent.AXIS_PRESSURE, event.source)
        val hovering = when (event.actionMasked) {
            MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE -> true
            MotionEvent.ACTION_BUTTON_PRESS, MotionEvent.ACTION_BUTTON_RELEASE -> reading?.hovering ?: (event.getPressure(index) == 0f)
            else -> false
        }
        reading = Reading(
            event.device?.name ?: "S Pen",
            if (range == null) null else StylusScrollDynamics.normalizePressure(event.getPressure(index), range.min, range.max),
            axis(MotionEvent.AXIS_TILT),
            axis(MotionEvent.AXIS_ORIENTATION),
            axis(MotionEvent.AXIS_DISTANCE),
            hovering,
            event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY != 0,
        )
    }
}
