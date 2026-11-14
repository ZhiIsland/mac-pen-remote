package io.nekohasekai.screensharing.sessionui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StylusScrollDynamicsTest {
    private fun StylusScrollDynamics.sample(x: Float = 0f, y: Float = 10f, time: Long = 100, pressure: Float? = null, hover: Boolean = false, lock: Boolean = false, density: Float = 1f) = apply(x, y, time, density, pressure, hover, lock)

    @Test fun disabledFeaturesPreserveOriginalScroll() {
        val dynamics = StylusScrollDynamics()
        assertEquals(StylusButtonGesture.Delta(4f, -12f), dynamics.sample(x = 4f, y = -12f))
    }

    @Test fun sustainedLightAndFirmPressureProduceDifferentSpeeds() {
        val light = StylusScrollDynamics()
        val firm = StylusScrollDynamics()
        var slow = 0f
        var fast = 0f
        repeat(20) {
            slow = light.sample(time = it * 16L, pressure = 0.05f).y
            fast = firm.sample(time = it * 16L, pressure = 0.9f).y
        }
        assertTrue(slow < 10f)
        assertTrue(fast > 10f)
        assertTrue(fast > slow * 2)
        assertTrue(fast <= 22.5f)
    }

    @Test fun pressureTransitionIsSmoothedAndResetDoesNotCarryItIntoNextGesture() {
        val dynamics = StylusScrollDynamics()
        repeat(20) { dynamics.sample(time = it * 16L, pressure = 0f) }
        val transition = dynamics.sample(time = 320L, pressure = 1f).y
        assertTrue(transition in 5f..15f)
        dynamics.reset()
        assertEquals(10f, dynamics.sample().y, 0.001f)
    }

    @Test fun hoverAccelerationDependsOnMovementSpeed() {
        val slow = StylusScrollDynamics().sample(y = 1f, hover = true).y
        val fast = StylusScrollDynamics().sample(y = 30f, hover = true).y / 30f
        assertEquals(1f, slow, 0.001f)
        assertTrue(fast > 1f && fast <= 2.5f)
    }

    @Test fun hoverAccelerationUsesDensityIndependentSpeed() {
        val first = StylusScrollDynamics().sample(y = 20f, hover = true, density = 1f).y
        val second = StylusScrollDynamics().sample(y = 60f, hover = true, density = 3f).y
        assertEquals(first, second / 3f, 0.001f)
    }

    @Test fun directionLockRejectsSidewaysJitterUntilNextGesture() {
        val dynamics = StylusScrollDynamics()
        assertEquals(0f, dynamics.sample(x = 2f, y = 20f, lock = true).x, 0f)
        assertEquals(0f, dynamics.sample(x = 30f, y = 0f, time = 116, lock = true).x, 0f)
        dynamics.reset()
        assertEquals(30f, dynamics.sample(x = 30f, y = 2f, lock = true).x, 0f)
        assertEquals(0f, dynamics.sample(x = 30f, y = 2f, lock = true).y, 0f)
    }

    @Test fun diagonalMotionAndDisabledLockKeepBothAxes() {
        val diagonal = StylusScrollDynamics().sample(x = 15f, y = 15f, lock = true)
        assertEquals(15f, diagonal.x, 0f)
        assertEquals(15f, diagonal.y, 0f)
        val free = StylusScrollDynamics().sample(x = 2f, y = 20f, lock = false)
        assertEquals(2f, free.x, 0f)
    }

    @Test fun invalidSensorsCannotInjectNonFiniteScroll() {
        assertNull(StylusScrollDynamics.normalizePressure(Float.NaN, 0f, 1f))
        assertNull(StylusScrollDynamics.normalizePressure(1f, 1f, 1f))
        val dynamics = StylusScrollDynamics()
        assertEquals(10f, dynamics.sample(pressure = Float.NaN).y, 0f)
        assertEquals(StylusButtonGesture.Delta(0f, 0f), dynamics.sample(y = Float.POSITIVE_INFINITY))
        assertTrue(dynamics.sample(time = 0L, hover = true).y.isFinite())
    }

    @Test fun pressureUsesDeviceRangeAndClampsOutliers() {
        assertEquals(0.5f, StylusScrollDynamics.normalizePressure(2048f, 0f, 4096f)!!, 0f)
        assertEquals(0f, StylusScrollDynamics.normalizePressure(-2f, 0f, 1f)!!, 0f)
        assertEquals(1f, StylusScrollDynamics.normalizePressure(2f, 0f, 1f)!!, 0f)
        assertEquals(0.5f, StylusScrollDynamics.normalizePressure(0.5f, null, null)!!, 0f)
    }
}
