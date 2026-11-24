package com.example.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDecisionPolicyTest {

    @Test
    fun `high priority update uses immediate flow when allowed`() {
        assertTrue(
            UpdateDecisionPolicy.shouldUseImmediateUpdate(
                updatePriority = 4,
                stalenessDays = 0,
                isImmediateAllowed = true
            )
        )
        assertTrue(
            UpdateDecisionPolicy.shouldUseImmediateUpdate(
                updatePriority = 5,
                stalenessDays = null,
                isImmediateAllowed = true
            )
        )
    }

    @Test
    fun `stale update at or above 7 days uses immediate flow when allowed`() {
        assertTrue(
            UpdateDecisionPolicy.shouldUseImmediateUpdate(
                updatePriority = 1,
                stalenessDays = 7,
                isImmediateAllowed = true
            )
        )
    }

    @Test
    fun `normal priority recent update uses flexible flow`() {
        assertFalse(
            UpdateDecisionPolicy.shouldUseImmediateUpdate(
                updatePriority = 2,
                stalenessDays = 3,
                isImmediateAllowed = true
            )
        )
        assertFalse(
            UpdateDecisionPolicy.shouldUseImmediateUpdate(
                updatePriority = 0,
                stalenessDays = null,
                isImmediateAllowed = true
            )
        )
    }

    @Test
    fun `immediate update is false when platform disallows immediate type`() {
        assertFalse(
            UpdateDecisionPolicy.shouldUseImmediateUpdate(
                updatePriority = 5,
                stalenessDays = 30,
                isImmediateAllowed = false
            )
        )
    }

    @Test
    fun `calculateProgressPercent handles valid, zero, and clamped values`() {
        assertNull(UpdateDecisionPolicy.calculateProgressPercent(100L, 0L))
        assertNull(UpdateDecisionPolicy.calculateProgressPercent(0L, -5L))
        assertEquals(0, UpdateDecisionPolicy.calculateProgressPercent(0L, 1000L))
        assertEquals(45, UpdateDecisionPolicy.calculateProgressPercent(450L, 1000L))
        assertEquals(100, UpdateDecisionPolicy.calculateProgressPercent(1200L, 1000L))
    }
}
