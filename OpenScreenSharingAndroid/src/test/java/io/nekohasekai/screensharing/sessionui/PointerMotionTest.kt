package io.nekohasekai.screensharing.sessionui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PointerMotionTest {
    @Test fun accelerationCanBeDisabledForPreciseMovementAtAnySpeed() {
        for (velocity in listOf(0f, 20f, 1000f, 100000f)) {
            assertEquals(0.75f, PointerMotion.gain(velocity, 3f, 0.75f, false), 0.0001f)
        }
    }

    @Test fun equivalentPhysicalMovementHasTheSameGainAcrossDensities() {
        val baseline = PointerMotion.gain(1000f, 1f, 1f, true)
        assertEquals(baseline, PointerMotion.gain(3000f, 3f, 1f, true), 0.0001f)
    }

    @Test fun theSpeedSettingScalesBothSlowAndFastMovement() {
        for (velocity in listOf(0f, 1000f, 100000f)) {
            val normal = PointerMotion.gain(velocity, 2f, 1f, true)
            assertEquals(normal / 2, PointerMotion.gain(velocity, 2f, 0.5f, true), 0.0001f)
            assertEquals(normal * 2, PointerMotion.gain(velocity, 2f, 2f, true), 0.0001f)
        }
    }

    @Test fun fastMotionIsBoundedAndSlowMotionKeepsThePreviousDefault() {
        assertEquals(1.2f, PointerMotion.gain(0f, 2f, 1f, true), 0.0001f)
        assertEquals(PointerMotion.gain(10000f, 2f, 1f, true), PointerMotion.gain(Float.MAX_VALUE, 2f, 1f, true), 0.0001f)
    }

    @Test fun invalidPreferencesAndSensorValuesCannotProduceAnInvalidPointerPosition() {
        for (velocity in listOf(Float.NaN, Float.POSITIVE_INFINITY, -10f)) {
            for (density in listOf(Float.NaN, 0f, -1f, Float.POSITIVE_INFINITY)) {
                val gain = PointerMotion.gain(velocity, density, Float.NaN, true)
                assertTrue(gain.isFinite() && gain > 0)
            }
        }
        assertTrue(PointerMotion.gain(0f, 1f, -10f, false) > 0)
        assertTrue(PointerMotion.gain(0f, 1f, Float.MAX_VALUE, false) <= 3f)
    }
}
