package io.nekohasekai.screensharing.sessionui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StylusButtonGestureTest {
    @Test fun tapKeepsRightClick() {
        val gesture = StylusButtonGesture(8f)
        gesture.begin(1, 10f, 10f)
        assertNull(gesture.move(1, 13f, 14f))
        assertTrue(gesture.finish(1, false))
        assertFalse(gesture.finish(1, false))
    }

    @Test fun verticalDragScrollsWithoutRightClick() {
        val gesture = StylusButtonGesture(8f)
        gesture.begin(1, 10f, 10f)
        assertEquals(StylusButtonGesture.Delta(0f, 15f), gesture.move(1, 10f, 25f))
        assertEquals(StylusButtonGesture.Delta(0f, -5f), gesture.move(1, 10f, 20f))
        assertFalse(gesture.finish(1, false))
    }

    @Test fun horizontalDragAndReturnNeverClick() {
        val gesture = StylusButtonGesture(8f)
        gesture.begin(1, 10f, 10f)
        assertEquals(StylusButtonGesture.Delta(20f, 0f), gesture.move(1, 30f, 10f))
        assertEquals(StylusButtonGesture.Delta(-20f, 0f), gesture.move(1, 10f, 10f))
        assertFalse(gesture.finish(1, false))
    }

    @Test fun cancellationNeverClicksAndOtherFingersCannotEndPen() {
        val gesture = StylusButtonGesture(8f)
        gesture.begin(3, 10f, 10f)
        assertNull(gesture.move(7, 100f, 100f))
        assertFalse(gesture.finish(7, false))
        assertTrue(gesture.owns(3))
        assertFalse(gesture.finish(3, true))
        assertFalse(gesture.owns(3))
    }

    @Test fun detachingCancelsPendingRightClick() {
        val gesture = StylusButtonGesture(8f)
        gesture.begin(1, 10f, 10f)
        gesture.cancel()
        assertFalse(gesture.finish(1, false))
    }
}
