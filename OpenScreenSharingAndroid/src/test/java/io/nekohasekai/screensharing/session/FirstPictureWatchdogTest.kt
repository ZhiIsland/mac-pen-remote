package io.nekohasekai.screensharing.session

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirstPictureWatchdogTest {
    @Test fun missingPictureTriggersRecoveryExactlyOnce() = runTest {
        var calls = 0
        FirstPictureWatchdog(this).start({ false }, { true }) { calls++ }
        advanceTimeBy(11_999)
        runCurrent()
        assertEquals(0, calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(30_000)
        assertEquals(1, calls)
    }

    @Test fun displayedPictureAndDisconnectCancelRecovery() = runTest {
        var calls = 0
        val watchdog = FirstPictureWatchdog(this)
        watchdog.start({ false }, { true }) { calls++ }
        advanceTimeBy(1_000)
        watchdog.cancel()
        advanceTimeBy(20_000)
        assertEquals(0, calls)
    }

    @Test fun obsoleteSessionDoesNotRecoverOverNewSession() = runTest {
        var pending = true
        var calls = 0
        FirstPictureWatchdog(this).start({ false }, { pending }) { calls++ }
        advanceTimeBy(1_000)
        pending = false
        advanceTimeBy(20_000)
        assertEquals(0, calls)
    }

    @Test fun backgroundSessionWaitsUntilForeground() = runTest {
        var background = true
        var calls = 0
        FirstPictureWatchdog(this).start({ background }, { true }) { calls++ }
        advanceTimeBy(15_000)
        runCurrent()
        assertEquals(0, calls)
        background = false
        advanceTimeBy(500)
        runCurrent()
        assertEquals(1, calls)
    }
}
