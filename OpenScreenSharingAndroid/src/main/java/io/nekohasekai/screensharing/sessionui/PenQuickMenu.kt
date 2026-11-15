package io.nekohasekai.screensharing.sessionui

import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.compose.ui.geometry.Offset
import io.nekohasekai.screensharing.R
import io.nekohasekai.screensharing.model.AppSettings
import io.nekohasekai.screensharing.model.PenMenuAction
import io.nekohasekai.screensharing.model.QuickInputs
import io.nekohasekai.screensharing.session.PointerButtons
import io.nekohasekai.screensharing.session.SessionController
import io.nekohasekai.screensharing.session.SystemCommand

internal class PenQuickMenu(private val view: RemoteScreenView, private val controller: SessionController) {
    private val anchor = View(view.context).apply {
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        view.addView(this, FrameLayout.LayoutParams(1, 1))
    }
    private var popup: PopupMenu? = null

    fun show(location: Offset) {
        dismiss()
        val run = controller.presentationRun
        val remotePoint = view.surface.imagePoint(location)
        anchor.x = location.x.coerceIn(0f, (view.width - 1).coerceAtLeast(0).toFloat())
        anchor.y = location.y.coerceIn(0f, (view.height - 1).coerceAtLeast(0).toFloat())
        val menu = PopupMenu(view.context, anchor)
        val actions = AppSettings.penMenuActions.distinct().ifEmpty { PenMenuAction.defaults }
        val inputs = QuickInputs.inputs.filter { it.steps(controller.mappingTarget).isNotEmpty() }
        val wasDragging = controller.isDragLocked
        actions.forEachIndexed { index, action ->
            if (action == PenMenuAction.QuickInputs) {
                val submenu = menu.menu.addSubMenu(0, index, index, action.title)
                inputs.forEachIndexed { inputIndex, input -> submenu.add(1, 1000 + inputIndex, inputIndex, input.name) }
                submenu.add(2, 9000, inputs.size, R.string.control_edit_quick_inputs)
            } else {
                val title = when (action) {
                    PenMenuAction.DragLock -> view.context.getString(if (wasDragging) R.string.control_end_drag else action.title)
                    PenMenuAction.FingerMode -> view.context.getString(R.string.control_finger_mode_value, view.context.getString(AppSettings.fingerInputMode.title))
                    else -> view.context.getString(action.title)
                }
                val item = menu.menu.add(0, index, index, title)
                item.isEnabled = when (action) {
                    PenMenuAction.MissionControl -> controller.canPerform(SystemCommand.MissionControl)
                    PenMenuAction.ShowDesktop -> controller.canPerform(SystemCommand.ShowDesktop)
                    else -> true
                }
            }
        }
        menu.setOnMenuItemClickListener { item ->
            if (view.isAttachedToWindow && controller.isControlling && controller.presentationRun == run) {
                if (item.itemId == 9000) {
                    controller.isEditingQuickInputs = true
                } else if (item.groupId == 1) {
                    inputs.getOrNull(item.itemId - 1000)?.let { controller.perform(it) }
                } else {
                    when (val action = actions.getOrNull(item.itemId)) {
                        PenMenuAction.RightClick, PenMenuAction.MiddleClick -> {
                            controller.movePointer(remotePoint)
                            controller.click(if (action == PenMenuAction.RightClick) PointerButtons.Secondary else PointerButtons.Middle)
                        }
                        PenMenuAction.DragLock -> {
                            controller.movePointer(remotePoint)
                            controller.setDragLocked(!wasDragging)
                        }
                        PenMenuAction.Keyboard -> controller.toggleKeyboard()
                        PenMenuAction.Toolbar -> controller.toggleToolbar()
                        PenMenuAction.MousePanel -> controller.setMousePanelVisible(!AppSettings.showsMousePanel)
                        PenMenuAction.FingerMode -> controller.toggleFingerInputMode()
                        PenMenuAction.MissionControl -> controller.perform(SystemCommand.MissionControl)
                        PenMenuAction.ShowDesktop -> controller.perform(SystemCommand.ShowDesktop)
                        PenMenuAction.QuickInputs, null -> Unit
                        else -> controller.performEditingAction(action)
                    }
                }
            }
            true
        }
        menu.setOnDismissListener {
            if (popup === menu) popup = null
            view.takeKeyFocus()
        }
        popup = menu
        menu.show()
    }

    fun dismiss() {
        popup?.dismiss()
        popup = null
    }
}
