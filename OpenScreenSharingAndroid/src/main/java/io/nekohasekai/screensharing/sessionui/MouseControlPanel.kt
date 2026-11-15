package io.nekohasekai.screensharing.sessionui

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.nekohasekai.screensharing.R
import io.nekohasekai.screensharing.model.AppSettings
import io.nekohasekai.screensharing.session.PointerButtons
import io.nekohasekai.screensharing.session.SessionController
import kotlin.math.roundToInt

@Composable
internal fun MouseControlPanel(controller: SessionController, view: RemoteScreenView) {
    val run = controller.presentationRun
    fun acceptsInput() = controller.isControlling && controller.hasWindowFocus && controller.presentationRun == run
    var fractionX by remember(controller) { mutableFloatStateOf(AppSettings.mousePanelX.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 1f) }
    var fractionY by remember(controller) { mutableFloatStateOf(AppSettings.mousePanelY.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.8f) }
    var panelSize by remember { mutableStateOf(IntSize.Zero) }
    DisposableEffect(controller, view, run) {
        onDispose {
            view.input.endPanelScroll(cancelled = true)
            controller.setDragLocked(false)
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        val travelX = (constraints.maxWidth - panelSize.width).coerceAtLeast(0)
        val travelY = (constraints.maxHeight - panelSize.height).coerceAtLeast(0)
        val panelWidth = minOf(300.dp, maxWidth)
        val panelHeight = maxHeight
        val padDescription = stringResource(R.string.control_panel_pad)
        val scrollDescription = stringResource(R.string.control_panel_scroll)
        val moveDescription = stringResource(R.string.control_move_panel)
        Surface(
            modifier = Modifier
                .offset { IntOffset((fractionX * travelX).roundToInt(), (fractionY * travelY).roundToInt()) }
                .width(panelWidth)
                .onSizeChanged { panelSize = it },
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
        ) {
            Column(
                Modifier.heightIn(max = panelHeight).verticalScroll(rememberScrollState()).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth()
                        .semantics { contentDescription = moveDescription }
                        .pointerInput(controller, travelX, travelY) {
                            detectDragGestures(
                                onDragEnd = {
                                    AppSettings.mousePanelX = fractionX
                                    AppSettings.mousePanelY = fractionY
                                },
                            ) { change, amount ->
                                change.consume()
                                if (travelX > 0) fractionX = (fractionX + amount.x / travelX).coerceIn(0f, 1f)
                                if (travelY > 0) fractionY = (fractionY + amount.y / travelY).coerceIn(0f, 1f)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.DragIndicator, null)
                    Text(stringResource(R.string.control_panel_title), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    IconButton(
                        onClick = { if (acceptsInput()) controller.toggleKeyboard() },
                        modifier = Modifier.focusProperties { canFocus = false },
                    ) { Icon(Icons.Outlined.Keyboard, stringResource(R.string.settings_keyboard)) }
                    IconButton(
                        onClick = { controller.setMousePanelVisible(false) },
                        modifier = Modifier.focusProperties { canFocus = false },
                    ) { Icon(Icons.Filled.Close, stringResource(R.string.control_close_panel)) }
                }
                if (controller.isDragLocked) {
                    Text(stringResource(R.string.control_drag_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.weight(1f).height(72.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
                            .semantics { contentDescription = padDescription }
                            .pointerInput(controller, run) {
                                var lastTime = 0L
                                detectDragGestures(
                                    onDragStart = {
                                        lastTime = SystemClock.uptimeMillis()
                                        if (acceptsInput()) controller.stopScrollMomentum()
                                    },
                                ) { change, amount ->
                                    change.consume()
                                    val now = SystemClock.uptimeMillis()
                                    val elapsed = (now - lastTime).coerceAtLeast(1)
                                    lastTime = now
                                    if (acceptsInput()) view.input.movePointer(amount, amount.getDistance() * 1000f / elapsed)
                                }
                            }
                            .pointerInput(controller, run) {
                                detectTapGestures(onTap = { if (acceptsInput()) controller.click(PointerButtons.Primary) })
                            },
                        contentAlignment = Alignment.Center,
                    ) { Text(padDescription, Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall) }
                    Box(
                        Modifier.width(52.dp).height(72.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
                            .semantics { contentDescription = scrollDescription }
                            .pointerInput(controller, run) {
                                detectDragGestures(
                                    onDragEnd = { if (acceptsInput()) view.input.endPanelScroll() },
                                    onDragCancel = { if (controller.presentationRun == run) view.input.endPanelScroll(cancelled = true) },
                                ) { change, amount ->
                                    change.consume()
                                    if (acceptsInput()) view.input.scrollFromPanel(Offset(0f, amount.y))
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) { Text(scrollDescription, style = MaterialTheme.typography.labelSmall) }
                }
                Row(Modifier.fillMaxWidth()) {
                    listOf(
                        PointerButtons.Primary to if (controller.isDragLocked) R.string.control_end_drag else R.string.control_left_button,
                        PointerButtons.Secondary to R.string.control_right_button,
                        PointerButtons.Middle to R.string.control_middle_button,
                    ).forEach { (button, title) ->
                        TextButton(
                            onClick = { if (acceptsInput()) controller.click(button) },
                            modifier = Modifier.weight(1f).focusProperties { canFocus = false },
                        ) { Text(stringResource(title)) }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { if (acceptsInput()) controller.setDragLocked(!controller.isDragLocked) },
                        modifier = Modifier.weight(1f).focusProperties { canFocus = false },
                    ) { Text(stringResource(if (controller.isDragLocked) R.string.control_end_drag else R.string.control_drag_lock)) }
                    TextButton(
                        onClick = { if (acceptsInput()) controller.toggleFingerInputMode() },
                        modifier = Modifier.weight(1f).focusProperties { canFocus = false },
                    ) { Text(stringResource(AppSettings.fingerInputMode.title)) }
                }
            }
        }
    }
}
