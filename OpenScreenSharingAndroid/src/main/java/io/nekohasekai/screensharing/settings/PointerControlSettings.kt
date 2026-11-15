package io.nekohasekai.screensharing.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Mouse
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.nekohasekai.screensharing.R
import io.nekohasekai.screensharing.app.ChoiceDialog
import io.nekohasekai.screensharing.app.ListGroup
import io.nekohasekai.screensharing.app.PageScaffold
import io.nekohasekai.screensharing.app.SectionFooter
import io.nekohasekai.screensharing.app.SectionHeader
import io.nekohasekai.screensharing.app.SwitchRow
import io.nekohasekai.screensharing.app.groupItemColors
import io.nekohasekai.screensharing.model.AppSettings
import io.nekohasekai.screensharing.model.FingerInputMode
import io.nekohasekai.screensharing.model.PenMenuAction

@Composable
internal fun PointerControlSettings(onEditMenu: () -> Unit) {
    var pickingMode by remember { mutableStateOf(false) }
    var pickingSpeed by remember { mutableStateOf(false) }
    SectionHeader(stringResource(R.string.control_input_title))
    ListGroup {
        ListItem(
            headlineContent = { Text(stringResource(R.string.control_finger_mode)) },
            supportingContent = { Text(stringResource(AppSettings.fingerInputMode.title)) },
            colors = groupItemColors(),
            modifier = Modifier.clickable { pickingMode = true },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.control_pointer_speed)) },
            supportingContent = { Text((AppSettings.pointerSpeed * 100).toInt().toString() + "%") },
            colors = groupItemColors(),
            modifier = Modifier.clickable { pickingSpeed = true },
        )
        SwitchRow(stringResource(R.string.control_pointer_acceleration), AppSettings.pointerAccelerates, { AppSettings.pointerAccelerates = it })
        SwitchRow(stringResource(R.string.control_mouse_panel), AppSettings.showsMousePanel, { AppSettings.showsMousePanel = it }, icon = Icons.Outlined.Mouse)
        SettingsLink(Icons.Outlined.Tune, stringResource(R.string.control_edit_pen_menu), onEditMenu)
    }
    SectionFooter(stringResource(R.string.control_input_help))
    if (pickingMode) {
        ChoiceDialog(
            stringResource(R.string.control_finger_mode),
            FingerInputMode.entries.map { it to stringResource(it.title) },
            AppSettings.fingerInputMode,
            onSelect = {
                AppSettings.fingerInputMode = it
                pickingMode = false
            },
            onDismiss = { pickingMode = false },
        )
    }
    if (pickingSpeed) {
        ChoiceDialog(
            stringResource(R.string.control_pointer_speed),
            listOf(0.5f, 0.75f, 1f, 1.5f, 2f).map { it to ((it * 100).toInt().toString() + "%") },
            AppSettings.pointerSpeed,
            onSelect = {
                AppSettings.pointerSpeed = it
                pickingSpeed = false
            },
            onDismiss = { pickingSpeed = false },
        )
    }
}

@Composable
internal fun PenMenuSettings(navigationIcon: @Composable () -> Unit) {
    PageScaffold(title = stringResource(R.string.control_edit_pen_menu), navigationIcon = navigationIcon) {
        SectionFooter(stringResource(R.string.control_pen_menu_help))
        val selected = AppSettings.penMenuActions.distinct().ifEmpty { PenMenuAction.defaults }
        ListGroup {
            (selected + PenMenuAction.entries.filter { it !in selected }).forEach { action ->
                val title = stringResource(action.title)
                val index = selected.indexOf(action)
                fun toggle() {
                    AppSettings.penMenuActions = if (index < 0) {
                        selected + action
                    } else if (selected.size > 1) {
                        selected - action
                    } else {
                        selected
                    }
                }
                ListItem(
                    headlineContent = { Text(title) },
                    leadingContent = { Checkbox(checked = index >= 0, onCheckedChange = { toggle() }) },
                    trailingContent = if (index < 0) {
                        null
                    } else {
                        (
                            {
                                Row {
                                    IconButton(enabled = index > 0, onClick = {
                                        AppSettings.penMenuActions = selected.toMutableList().apply { add(index - 1, removeAt(index)) }
                                    }) { Icon(Icons.Outlined.ArrowUpward, stringResource(R.string.control_move_up, title)) }
                                    IconButton(enabled = index < selected.lastIndex, onClick = {
                                        AppSettings.penMenuActions = selected.toMutableList().apply { add(index + 1, removeAt(index)) }
                                    }) { Icon(Icons.Outlined.ArrowDownward, stringResource(R.string.control_move_down, title)) }
                                }
                            }
                            )
                    },
                    colors = groupItemColors(),
                    modifier = Modifier.clickable { toggle() },
                )
            }
        }
        TextButton(onClick = { AppSettings.penMenuActions = PenMenuAction.defaults }) { Text(stringResource(R.string.control_reset_menu)) }
    }
}
