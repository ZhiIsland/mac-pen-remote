package io.nekohasekai.screensharing.settings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.nekohasekai.screensharing.R
import io.nekohasekai.screensharing.app.ListGroup
import io.nekohasekai.screensharing.app.PageScaffold
import io.nekohasekai.screensharing.app.SectionFooter
import io.nekohasekai.screensharing.app.groupItemColors
import io.nekohasekai.screensharing.sessionui.PenInputTelemetry
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun PenDiagnosticsScreen(navigationIcon: @Composable () -> Unit) {
    val reading = PenInputTelemetry.reading
    val unavailable = stringResource(R.string.pen_diag_unreported)
    fun number(value: Float?): String = value?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: unavailable
    PageScaffold(stringResource(R.string.pen_diagnostics), navigationIcon) {
        SectionFooter(stringResource(R.string.pen_diag_help))
        ListGroup {
            AndroidView(factory = { PenTestView(it) }, modifier = Modifier.fillMaxWidth().height(210.dp))
        }
        ListGroup {
            val rows = listOf(
                R.string.pen_diag_device to (reading?.device ?: stringResource(R.string.pen_diag_waiting)),
                R.string.pen_diag_pressure to (reading?.pressure?.let { "${(it * 100).roundToInt()}%" } ?: unavailable),
                R.string.pen_diag_tilt to number(reading?.tilt?.let { it * 180f / Math.PI.toFloat() }),
                R.string.pen_diag_orientation to number(reading?.orientation?.let { it * 180f / Math.PI.toFloat() }),
                R.string.pen_diag_distance to number(reading?.distance),
                R.string.pen_diag_button to stringResource(if (reading?.sideButton == true) R.string.pen_diag_pressed else R.string.pen_diag_released),
            )
            rows.forEach { (title, value) ->
                ListItem(headlineContent = { Text(stringResource(title)) }, supportingContent = { Text(value) }, colors = groupItemColors())
            }
        }
        SectionFooter(stringResource(R.string.pen_diag_footer))
    }
}

@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
private class PenTestView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private var penX = -1f
    private var penY = -1f
    private var pressure = 0f
    private var contact = false

    init {
        contentDescription = context.getString(R.string.pen_diag_help)
    }

    private fun record(event: MotionEvent): Boolean {
        val index = event.actionIndex.coerceAtMost(event.pointerCount - 1)
        if (event.getToolType(index) != MotionEvent.TOOL_TYPE_STYLUS && event.getToolType(index) != MotionEvent.TOOL_TYPE_ERASER) return true
        PenInputTelemetry.record(event, index, force = true)
        penX = event.getX(index)
        penY = event.getY(index)
        if (event.actionMasked != MotionEvent.ACTION_BUTTON_PRESS && event.actionMasked != MotionEvent.ACTION_BUTTON_RELEASE) {
            contact = event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN
        }
        pressure = if (contact) PenInputTelemetry.reading?.pressure ?: 0f else 0f
        if (event.actionMasked == MotionEvent.ACTION_DOWN) parent?.requestDisallowInterceptTouchEvent(true)
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) parent?.requestDisallowInterceptTouchEvent(false)
        if (event.actionMasked == MotionEvent.ACTION_HOVER_EXIT || event.actionMasked == MotionEvent.ACTION_CANCEL) penX = -1f
        invalidate()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = record(event)
    override fun onHoverEvent(event: MotionEvent): Boolean = record(event)
    override fun onGenericMotionEvent(event: MotionEvent): Boolean = record(event)

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(25, 35, 47))
        paint.color = Color.rgb(65, 83, 100)
        paint.strokeWidth = density
        val step = 24f * density
        var x = step
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
            x += step
        }
        var y = step
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
            y += step
        }
        if (penX >= 0f) {
            paint.color = if (contact) Color.rgb(90, 220, 195) else Color.WHITE
            canvas.drawCircle(penX, penY, (4f + pressure * 16f) * density, paint)
        }
        paint.color = Color.rgb(90, 220, 195)
        canvas.drawRect(0f, height - 6f * density, width * pressure, height.toFloat(), paint)
    }
}
