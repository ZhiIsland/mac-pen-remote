package io.nekohasekai.screensharing.sessionui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StylusPalmGuardTest {
    @Test fun fingersWorkBeforePenArrives() {
        assertFalse(StylusPalmGuard().rejectsFinger(0))
    }

    @Test fun hoveringBlocksFingersEvenWhenPenIsStationary() {
        val guard = StylusPalmGuard()
        guard.hover(true, 100)
        assertTrue(guard.rejectsFinger(10_000))
    }

    @Test fun hoverExitBridgesPenDownButExpiresForFingerUse() {
        val guard = StylusPalmGuard()
        guard.hover(true, 100)
        guard.hover(false, 200)
        assertTrue(guard.rejectsFinger(250))
        assertFalse(guard.rejectsFinger(351))
        guard.contact(3, true, 300)
        assertTrue(guard.rejectsFinger(1000))
    }

    @Test fun contactReleaseHasOnlyShortGracePeriod() {
        val guard = StylusPalmGuard()
        guard.contact(3, true, 100)
        guard.contact(3, false, 200)
        assertTrue(guard.rejectsFinger(350))
        assertFalse(guard.rejectsFinger(351))
    }

    @Test fun unrelatedContactCannotReleasePenAndFocusLossResetsEverything() {
        val guard = StylusPalmGuard()
        guard.contact(3, true, 100)
        guard.contact(7, false, 200)
        assertTrue(guard.rejectsFinger(1000))
        guard.hover(true, 1000)
        guard.reset()
        assertFalse(guard.rejectsFinger(1001))
    }
}
