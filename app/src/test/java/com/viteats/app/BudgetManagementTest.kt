package com.viteats.app

import org.junit.Assert.*
import org.junit.Test

class BudgetManagementTest {

    private fun calculateSpendingPercentage(currentSpending: Double, budgetLimit: Double): Float {
        if (budgetLimit <= 0) return 0f
        return ((currentSpending / budgetLimit) * 100.0).coerceAtLeast(0.0).toFloat()
    }

    private fun isApproachingBudgetLimit(currentSpending: Double, budgetLimit: Double): Boolean {
        return calculateSpendingPercentage(currentSpending, budgetLimit) >= 80f
    }

    @Test
    fun testUnderBudgetDoesNotTriggerWarning() {
        val spending = 1500.0
        val budget = 3000.0
        val percentage = calculateSpendingPercentage(spending, budget)

        assertEquals(50f, percentage, 0.001f)
        assertFalse(isApproachingBudgetLimit(spending, budget))
    }

    @Test
    fun testReaching80PercentTriggersWarning() {
        val spending = 2400.0
        val budget = 3000.0
        val percentage = calculateSpendingPercentage(spending, budget)

        assertEquals(80f, percentage, 0.001f)
        assertTrue(isApproachingBudgetLimit(spending, budget))
    }

    @Test
    fun testAbove80PercentTriggersWarning() {
        val spending = 2450.0
        val budget = 3000.0
        val percentage = calculateSpendingPercentage(spending, budget)

        assertTrue(percentage >= 80f)
        assertTrue(isApproachingBudgetLimit(spending, budget))
    }

    @Test
    fun testBudgetExceededTriggersWarning() {
        val spending = 3500.0
        val budget = 3000.0
        val percentage = calculateSpendingPercentage(spending, budget)

        assertTrue(percentage > 100f)
        assertTrue(isApproachingBudgetLimit(spending, budget))
    }

    @Test
    fun testZeroOrNegativeBudgetSafety() {
        val spending = 100.0
        val budget = 0.0
        val percentage = calculateSpendingPercentage(spending, budget)

        assertEquals(0f, percentage, 0.001f)
        assertFalse(isApproachingBudgetLimit(spending, budget))
    }

    @Test
    fun testBudgetDigitSanitization() {
        val rawInput = "₹ 3,500 abc"
        val sanitized = rawInput.filter { it.isDigit() }.take(6)
        assertEquals("3500", sanitized)
        assertEquals(3500.0, sanitized.toDouble(), 0.001)
    }
}
