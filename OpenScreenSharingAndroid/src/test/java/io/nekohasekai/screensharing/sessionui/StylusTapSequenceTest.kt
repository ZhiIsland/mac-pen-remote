package io.nekohasekai.screensharing.sessionui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StylusTapSequenceTest {
    @Test fun disabledDoubleClickHasNoDelay() {
        val sequence = StylusTapSequence(300, 10f)
        val result = sequence.tap(10f, 10f, 100, false)
        assertNotNull(result.single)
        assertFalse(result.waits)
        assertNull(sequence.timeout(500))
    }

    @Test fun doubleClickNeverAlsoFiresASingle() {
        val sequence = StylusTapSequence(300, 10f)
        assertTrue(sequence.tap(10f, 10f, 100, true).waits)
        val result = sequence.tap(12f, 11f, 350, true)
        assertTrue(result.double)
        assertNull(result.single)
        assertNull(sequence.timeout(1000))
    }

    @Test fun singleWaitsForDeadlineAndFiresOnce() {
        val sequence = StylusTapSequence(300, 10f)
        sequence.tap(10f, 10f, 100, true)
        assertNull(sequence.timeout(399))
        assertNotNull(sequence.timeout(400))
        assertNull(sequence.timeout(401))
    }

    @Test fun distantOrLateSecondTapIsNotADouble() {
        val sequence = StylusTapSequence(300, 10f)
        sequence.tap(10f, 10f, 100, true)
        val distant = sequence.tap(100f, 100f, 200, true)
        assertNotNull(distant.single)
        assertFalse(distant.double)
        assertTrue(distant.waits)
        val late = sequence.tap(100f, 100f, 600, true)
        assertNotNull(late.single)
        assertFalse(late.double)
    }

    @Test fun cancellationDropsPendingRemoteClick() {
        val sequence = StylusTapSequence(300, 10f)
        sequence.tap(10f, 10f, 100, true)
        sequence.cancel()
        assertNull(sequence.timeout(1000))
        assertFalse(sequence.tap(10f, 10f, 1100, true).double)
    }
}
