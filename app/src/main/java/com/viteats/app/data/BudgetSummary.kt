package com.viteats.app.data

import java.util.Calendar
import java.util.TimeZone

data class BudgetSummary(
    val limit: Double,
    val spent: Double,
    val remaining: Double?,
    val availableToSpend: Double,
    val daysLeft: Int,
    val safeDailySpend: Double,
    val dailyAverage: Double,
    val projectedMonthlySpend: Double,
    val usedFraction: Float
) {
    companion object {
        fun calculate(limit: Double, spent: Double, wallet: Double, today: Calendar = indiaCalendar()): BudgetSummary {
            val daysInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)
            val day = today.get(Calendar.DAY_OF_MONTH)
            val daysLeft = daysInMonth - day + 1
            val remaining = if (limit > 0) (limit - spent).coerceAtLeast(0.0) else null
            val available = minOf(wallet.coerceAtLeast(0.0), remaining ?: wallet.coerceAtLeast(0.0))
            val average = spent / day
            return BudgetSummary(
                limit, spent, remaining, available, daysLeft, available / daysLeft,
                average, average * daysInMonth,
                if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
            )
        }
    }
}

fun indiaCalendar(): Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
