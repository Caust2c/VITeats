package com.viteats.app

import com.viteats.app.data.BudgetSummary
import com.viteats.app.data.indiaCalendar
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class BudgetSummaryTest {
    private fun day(year: Int = 2026, month: Int = Calendar.OCTOBER, date: Int = 8) = indiaCalendar().apply {
        clear()
        set(year, month, date)
    }

    @Test
    fun walletTopUpsCannotIncreaseTheRemainingBudget() {
        val summary = BudgetSummary.calculate(3000.0, 1200.0, 9000.0, day())
        assertEquals(1800.0, summary.remaining!!, 0.001)
        assertEquals(75.0, summary.safeDailySpend, 0.001)
        assertEquals(24, summary.daysLeft)
        assertEquals(4650.0, summary.projectedMonthlySpend, 0.001)
    }

    @Test
    fun dailyAllowanceRespectsLowWalletBalance() {
        assertEquals(10.0, BudgetSummary.calculate(3000.0, 1200.0, 240.0, day()).safeDailySpend, 0.001)
    }

    @Test
    fun lastDayAndLeapYearIncludeToday() {
        val summary = BudgetSummary.calculate(1000.0, 700.0, 900.0, day(2028, Calendar.FEBRUARY, 29))
        assertEquals(1, summary.daysLeft)
        assertEquals(300.0, summary.safeDailySpend, 0.001)
    }

    @Test
    fun overspendingAndUnsetBudgetAreHandled() {
        val overspent = BudgetSummary.calculate(100.0, 120.0, 900.0, day())
        assertEquals(0.0, overspent.safeDailySpend, 0.001)
        assertEquals(1f, overspent.usedFraction)
        val unset = BudgetSummary.calculate(0.0, 120.0, 240.0, day())
        assertNull(unset.remaining)
        assertEquals(10.0, unset.safeDailySpend, 0.001)
    }
}
