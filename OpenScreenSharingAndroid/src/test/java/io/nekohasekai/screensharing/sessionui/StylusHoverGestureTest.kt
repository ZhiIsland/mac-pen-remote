package io.nekohasekai.screensharing.sessionui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StylusHoverGestureTest {
    private fun StylusHoverGesture.sample(pressed: Boolean, x: Float = 10f, y: Float = 10f, id: Int = 1, eligible: Boolean = true) = update(id, x, y, pressed, eligible)

    @Test fun duplicateGenericAndHoverEventsClickOnlyOnce() {
        val gesture = StylusHoverGesture(8f)
        assertTrue(gesture.sample(true).began)
        assertFalse(gesture.sample(true).began)
        val release = gesture.sample(false)
        assertTrue(release.ended)
        assertTrue(release.rightClick)
        assertFalse(gesture.sample(false).rightClick)
        assertFalse(gesture.isActive)
    }

    @Test fun handJitterKeepsRightClick() {
        val gesture = StylusHoverGesture(8f)
        gesture.sample(true)
        assertNull(gesture.sample(true, x = 13f, y = 14f).delta)
        assertTrue(gesture.sample(false, x = 13f, y = 14f).rightClick)
    }

    @Test fun hoverScrollAndReturnNeverRightClick() {
        val gesture = StylusHoverGesture(8f)
        gesture.sample(true)
        assertEquals(StylusButtonGesture.Delta(20f, -15f), gesture.sample(true, x = 30f, y = -5f).delta)
        assertEquals(StylusButtonGesture.Delta(-20f, 15f), gesture.sample(true).delta)
        val release = gesture.sample(false)
        assertTrue(release.ended)
        assertFalse(release.cancelled)
        assertFalse(release.rightClick)
    }

    @Test fun finalMovementOnButtonReleaseScrollsInsteadOfClicking() {
        val gesture = StylusHoverGesture(8f)
        gesture.sample(true)
        val release = gesture.sample(false, y = 50f)
        assertEquals(StylusButtonGesture.Delta(0f, 40f), release.delta)
        assertTrue(release.ended)
        assertFalse(release.rightClick)
    }

    @Test fun hoverExitAndContactCannotGenerateAnExtraRightClick() {
        val hover = StylusHoverGesture(8f)
        val contact = StylusButtonGesture(8f)
        hover.sample(true)
        assertTrue(hover.blockUntilRelease().cancelled)
        contact.begin(1, 10f, 10f)
        assertTrue(contact.finish(1, cancelled = false))
        assertFalse(hover.sample(true).began)
        assertFalse(hover.sample(false).rightClick)
        assertTrue(hover.sample(true).began)
        assertTrue(hover.sample(false).rightClick)
    }

    @Test fun normalContactAlsoSuppressesCarriedHoverButton() {
        val gesture = StylusHoverGesture(8f)
        gesture.blockUntilRelease()
        assertFalse(gesture.sample(true).began)
        assertFalse(gesture.sample(false).rightClick)
        assertTrue(gesture.sample(true).began)
    }

    @Test fun hoverScrollThenTipContactNeverOpensContextMenu() {
        val hover = StylusHoverGesture(8f)
        val contact = StylusButtonGesture(8f)
        hover.sample(true)
        hover.sample(true, y = 30f)
        hover.blockUntilRelease() // Android sends HOVER_EXIT before ACTION_DOWN.
        val continuesScroll = hover.beginContact(pressed = true)
        assertTrue(continuesScroll)
        contact.begin(1, 10f, 30f, alreadyScrolling = continuesScroll)
        assertFalse(contact.finish(1, cancelled = false))
        assertFalse(hover.sample(true).began)
        assertFalse(hover.sample(false).rightClick)
    }

    @Test fun hoverScrollDoesNotAffectContactAfterButtonReleased() {
        val gesture = StylusHoverGesture(8f)
        gesture.sample(true)
        gesture.sample(true, y = 30f)
        gesture.sample(false, y = 30f)
        assertFalse(gesture.beginContact(pressed = true))
    }

    @Test fun focusLossOrLeavingPictureCancelsAndRequiresRelease() {
        val gesture = StylusHoverGesture(8f)
        gesture.sample(true)
        val lostFocus = gesture.sample(true, eligible = false)
        assertTrue(lostFocus.ended)
        assertTrue(lostFocus.cancelled)
        assertFalse(lostFocus.rightClick)
        assertFalse(gesture.sample(true).began)
        assertFalse(gesture.sample(false).rightClick)
        assertTrue(gesture.sample(true).began)
    }

    @Test fun otherPointerCannotCompletePendingRightClick() {
        val gesture = StylusHoverGesture(8f)
        gesture.sample(true)
        val otherPointer = gesture.sample(false, id = 7)
        assertTrue(otherPointer.cancelled)
        assertFalse(otherPointer.rightClick)
        assertFalse(gesture.isActive)
    }
}
