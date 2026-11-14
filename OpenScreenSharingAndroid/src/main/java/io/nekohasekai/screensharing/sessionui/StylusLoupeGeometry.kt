package io.nekohasekai.screensharing.sessionui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.min

internal object StylusLoupeGeometry {
    fun spot(area: Rect, image: Rect, point: Offset, density: Float): LoupeSpot? {
        if (area.width <= 0f || area.height <= 0f || !image.contains(point)) return null
        val margin = 12f * density
        val width = min(176f * density, area.width - margin * 2)
        val height = min(104f * density, area.height - margin * 2)
        if (width < 48f * density || height < 40f * density || image.width <= 0f || image.height <= 0f) return null
        val scale = maxOf(2.5f, width / image.width, height / image.height)
        val sourceWidth = width / scale
        val sourceHeight = height / scale
        val sourceLeft = (point.x - sourceWidth / 2).coerceIn(image.left, maxOf(image.left, image.right - sourceWidth))
        val sourceTop = (point.y - sourceHeight / 2).coerceIn(image.top, maxOf(image.top, image.bottom - sourceHeight))
        val gap = 36f * density
        val above = point.y - height - gap
        val below = point.y + gap
        val top = when {
            above >= area.top + margin -> above
            below + height <= area.bottom - margin -> below
            else -> area.top + margin
        }
        val left = (point.x - width / 2).coerceIn(area.left + margin, area.right - margin - width)
        return LoupeSpot(
            Rect(sourceLeft, sourceTop, sourceLeft + sourceWidth, sourceTop + sourceHeight),
            Rect(left, top, left + width, top + height),
        )
    }
}
