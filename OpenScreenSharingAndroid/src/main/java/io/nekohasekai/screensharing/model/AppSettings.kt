package io.nekohasekai.screensharing.model

import androidx.compose.runtime.mutableStateOf
import io.nekohasekai.screensharing.App
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import java.util.UUID
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object AppSettings {
    var penMenuActions: List<PenMenuAction> by Persisted("penMenuActions", serializer<List<PenMenuAction>>(), PenMenuAction.defaults)

    var fingerInputMode: FingerInputMode by Persisted("fingerInputMode", serializer<FingerInputMode>(), FingerInputMode.Trackpad)

    var pointerSpeed: Float by Persisted("pointerSpeed", serializer<Float>(), 1f)

    var pointerAccelerates: Boolean by Persisted("pointerAccelerates", serializer<Boolean>(), true)

    var showsMousePanel: Boolean by Persisted("showsMousePanel", serializer<Boolean>(), false)

    var mousePanelX: Float by Persisted("mousePanelX", serializer<Float>(), 1f)

    var mousePanelY: Float by Persisted("mousePanelY", serializer<Float>(), 0.8f)

    var stylusButtonScrolls: Boolean by Persisted("stylusButtonScrolls", serializer<Boolean>(), true)

    var stylusHoverScrolls: Boolean by Persisted("stylusHoverScrolls", serializer<Boolean>(), true)

    var stylusScrollSpeed: Float by Persisted("stylusScrollSpeed", serializer<Float>(), 1f)

    var stylusScrollReversed: Boolean by Persisted("stylusScrollReversed", serializer<Boolean>(), false)

    var stylusPressureScrolls: Boolean by Persisted("stylusPressureScrolls", serializer<Boolean>(), true)

    var stylusHoverAccelerates: Boolean by Persisted("stylusHoverAccelerates", serializer<Boolean>(), true)

    var stylusScrollLocksAxis: Boolean by Persisted("stylusScrollLocksAxis", serializer<Boolean>(), true)

    var stylusShowsLoupe: Boolean by Persisted("stylusShowsLoupe", serializer<Boolean>(), true)

    var stylusRejectsPalm: Boolean by Persisted("stylusRejectsPalm", serializer<Boolean>(), true)

    var stylusLongPressAction: PenShortcutAction by Persisted("stylusLongPressAction", serializer<PenShortcutAction>(), PenShortcutAction.QuickMenu)

    var stylusDoubleClickAction: PenShortcutAction by Persisted("stylusDoubleClickAction", serializer<PenShortcutAction>(), PenShortcutAction.None)

    var showsDebugInfo: Boolean by Persisted("showsDebugInfo", serializer<Boolean>(), false)

    var cursorStyle: CursorStyle by Persisted("cursorStyle", serializer<CursorStyle>(), CursorStyle.System)

    var pinchZooms: Boolean by Persisted("pinchZooms", serializer<Boolean>(), App.instance.resources.configuration.smallestScreenWidthDp < 600)

    var pictureFillsScreen: Boolean by Persisted("pictureFillsScreen", serializer<Boolean>(), true)

    var computerLayout: ComputerLayout by Persisted("computerLayout", serializer<ComputerLayout>(), ComputerLayout.Grid)

    var computerOrder: List<UUID> by Persisted("computerOrder", ListSerializer(UUIDSerializer), emptyList())

    var seenTutorials: Set<String> by Persisted("seenTutorials", serializer<Set<String>>(), emptySet())

    private class Persisted<T>(private val name: String, private val serializer: KSerializer<T>, initial: T) : ReadWriteProperty<Any, T> {
        private val state = mutableStateOf(PreferenceStore.value(name, serializer) ?: initial)

        override fun getValue(thisRef: Any, property: KProperty<*>): T = state.value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            state.value = value
            PreferenceStore.set(name, value, serializer)
        }
    }
}

@Serializable
enum class PenShortcutAction {
    None,
    QuickMenu,
    Keyboard,
    Toolbar,
    MissionControl,
    ShowDesktop,
    MousePanel,
    DragLock,
    FingerMode,
}

@Serializable
sealed interface CursorStyle {
    @Serializable
    @SerialName("system")
    data object System : CursorStyle

    @Serializable
    @SerialName("remote")
    data object Remote : CursorStyle

    @Serializable
    @SerialName("theme")
    data class Theme(val id: String) : CursorStyle
}

@Serializable
enum class ComputerLayout {
    @SerialName("grid")
    Grid,

    @SerialName("list")
    List,
}
