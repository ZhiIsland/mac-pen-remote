package io.nekohasekai.screensharing.sessionui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StylusLoupeGeometryTest {
    private val portrait = Rect(0f, 0f, 400f, 800f)

    @Test fun magnifierMapsSampleCenterToItsDisplayedCenter() {
        val point = Offset(200f, 400f)
        val spot = StylusLoupeGeometry.spot(portrait, portrait, point, 1f)!!
        assertEquals(2.5f, spot.magnification, 0.001f)
        assertEquals(spot.frame.center.x, spot.map(point).x, 0.001f)
        assertEquals(spot.frame.center.y, spot.map(point).y, 0.001f)
        assertTrue(spot.frame.bottom < point.y)
        assertEquals(spot.frame, spot.map(spot.source))
    }

    @Test fun edgeTargetsStayInsideSourceAndDisplayArea() {
        for (point in listOf(Offset(0f, 0f), Offset(399f, 0f), Offset(0f, 799f), Offset(399f, 799f))) {
            val spot = StylusLoupeGeometry.spot(portrait, portrait, point, 1f)!!
            assertTrue(spot.source.left >= portrait.left && spot.source.right <= portrait.right)
            assertTrue(spot.source.top >= portrait.top && spot.source.bottom <= portrait.bottom)
            assertTrue(spot.frame.left >= portrait.left && spot.frame.right <= portrait.right)
            assertTrue(spot.frame.top >= portrait.top && spot.frame.bottom <= portrait.bottom)
            assertTrue(spot.frame.inflate(0.01f).contains(spot.map(point)))
        }
    }

    @Test fun keyboardAndLandscapeReduceAvailableArea() {
        val available = Rect(10f, 20f, 790f, 200f)
        val spot = StylusLoupeGeometry.spot(available, Rect(0f, 0f, 800f, 400f), Offset(700f, 180f), 1f)!!
        assertTrue(spot.frame.bottom <= available.bottom)
        assertTrue(spot.frame.top >= available.top)
    }

    @Test fun tinyImageUsesHigherMagnificationWithoutSamplingOutside() {
        val image = Rect(190f, 390f, 210f, 410f)
        val spot = StylusLoupeGeometry.spot(portrait, image, image.center, 1f)!!
        assertTrue(spot.magnification >= 2.5f)
        assertTrue(spot.source.left >= image.left && spot.source.right <= image.right)
        assertTrue(spot.source.top >= image.top && spot.source.bottom <= image.bottom)
    }

    @Test fun invalidOrInsufficientAreaDoesNotCreateLoupe() {
        assertNull(StylusLoupeGeometry.spot(Rect.Zero, portrait, Offset(5f, 5f), 1f))
        assertNull(StylusLoupeGeometry.spot(Rect(0f, 0f, 20f, 20f), portrait, Offset(5f, 5f), 1f))
        assertNull(StylusLoupeGeometry.spot(portrait, portrait, Offset(-10f, -10f), 1f))
    }

    @Test fun existingOneToOneObstructionLoupeStillTranslatesWithoutScaling() {
        val source = Rect(10f, 20f, 110f, 70f)
        val frame = source.translate(150f, -10f)
        val spot = LoupeSpot(source, frame)
        assertEquals(1f, spot.magnification, 0f)
        assertEquals(frame, spot.map(source))
        assertNotNull(spot.map(Offset(20f, 30f)))
    }
}
