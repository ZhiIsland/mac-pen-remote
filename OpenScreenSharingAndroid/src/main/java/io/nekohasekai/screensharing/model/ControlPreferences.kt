package io.nekohasekai.screensharing.model

import androidx.annotation.StringRes
import io.nekohasekai.screensharing.R
import kotlinx.serialization.Serializable

@Serializable
enum class FingerInputMode(@get:StringRes val title: Int) {
    Trackpad(R.string.control_trackpad),
    Direct(R.string.control_direct),
}

@Serializable
enum class PenMenuAction(@get:StringRes val title: Int) {
    RightClick(R.string.pen_menu_right_click),
    MiddleClick(R.string.pen_menu_middle_click),
    DragLock(R.string.control_drag_lock),
    Copy(R.string.control_copy),
    Paste(R.string.control_paste),
    Cut(R.string.control_cut),
    Undo(R.string.control_undo),
    Redo(R.string.control_redo),
    SelectAll(R.string.control_select_all),
    Find(R.string.control_find),
    SwitchApp(R.string.control_switch_app),
    Escape(R.string.control_escape),
    Keyboard(R.string.pen_action_keyboard),
    Toolbar(R.string.pen_action_toolbar),
    MousePanel(R.string.control_mouse_panel),
    FingerMode(R.string.control_finger_mode),
    QuickInputs(R.string.settings_quick_input),
    MissionControl(R.string.pen_action_mission_control),
    ShowDesktop(R.string.pen_action_show_desktop),
    ;

    companion object {
        val defaults = listOf(RightClick, MiddleClick, DragLock, Copy, Paste, Undo, Keyboard, MousePanel, FingerMode, MissionControl, ShowDesktop)
    }
}
