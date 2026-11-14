package io.nekohasekai.screensharing.sessionui

import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.compose.ui.geometry.Offset
import io.nekohasekai.screensharing.R
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
        val entries = listOf(
            R.string.pen_menu_right_click,
            R.string.pen_menu_middle_click,
            R.string.pen_action_keyboard,
            R.string.pen_action_toolbar,
            R.string.pen_action_mission_control,
            R.string.pen_action_show_desktop,
        )
        entries.forEachIndexed { index, title -> menu.menu.add(0, index, index, title) }
        menu.menu.findItem(4).isEnabled = controller.canPerform(SystemCommand.MissionControl)
        menu.menu.findItem(5).isEnabled = controller.canPerform(SystemCommand.ShowDesktop)
        menu.setOnMenuItemClickListener { item ->
            if (view.isAttachedToWindow && controller.isControlling && controller.presentationRun == run) {
                when (item.itemId) {
                    0, 1 -> {
                        controller.movePointer(remotePoint)
                        controller.click(if (item.itemId == 0) PointerButtons.Secondary else PointerButtons.Middle)
                    }
                    2 -> controller.toggleKeyboard()
                    3 -> controller.toggleToolbar()
                    4 -> controller.perform(SystemCommand.MissionControl)
                    5 -> controller.perform(SystemCommand.ShowDesktop)
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
