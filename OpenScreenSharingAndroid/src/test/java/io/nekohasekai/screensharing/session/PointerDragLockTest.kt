package io.nekohasekai.screensharing.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PointerDragLockTest {
    @Test fun ordinaryButtonsAreUnchangedWithoutALock() {
        val lock = PointerDragLock()
        assertEquals(PointerButtons.None, lock.buttons(PointerButtons.None))
        assertEquals(PointerButtons.Secondary, lock.buttons(PointerButtons.Secondary))
    }

    @Test fun liftingAndRepositioningDoesNotDropALockedDrag() {
        val lock = PointerDragLock()
        lock.set(true)
        assertEquals(PointerButtons.Primary, lock.buttons(PointerButtons.Primary))
        assertEquals(PointerButtons.Primary, lock.buttons(PointerButtons.None))
        assertEquals(PointerButtons.Primary, lock.buttons(PointerButtons.Primary))
        assertEquals(PointerButtons.Primary, lock.buttons(PointerButtons.None))
    }

    @Test fun OtherPhysicalButtonsRemainIndependent() {
        val lock = PointerDragLock()
        lock.set(true)
        assertEquals(PointerButtons.Primary + PointerButtons.Middle + PointerButtons.Secondary, lock.buttons(PointerButtons.Middle + PointerButtons.Secondary))
        assertEquals(PointerButtons.Primary, lock.buttons(PointerButtons.None))
        lock.set(false)
        assertEquals(PointerButtons.Secondary, lock.buttons(PointerButtons.Secondary))
    }

    @Test fun focusOrSessionResetCannotCarryTheLockIntoTheNextSession() {
        val lock = PointerDragLock()
        lock.set(true)
        lock.clear()
        assertFalse(lock.active)
        assertEquals(PointerButtons.None, lock.buttons(PointerButtons.None))
        assertEquals(PointerButtons.Primary, lock.buttons(PointerButtons.Primary))
        assertEquals(PointerButtons.None, lock.buttons(PointerButtons.None))
    }

    @Test fun repeatedCancellationAndExplicitEndingAreSafe() {
        val lock = PointerDragLock()
        lock.set(true)
        lock.set(true)
        lock.set(false)
        lock.clear()
        lock.clear()
        assertEquals(PointerButtons.None, lock.buttons(PointerButtons.None))
        lock.set(true)
        assertEquals(PointerButtons.Primary, lock.buttons(PointerButtons.None))
    }
}
