package com.viteats.app.data

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

class BudgetManager(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences("viteats_budget_prefs", Context.MODE_PRIVATE)
    )

    companion object {
        private const val KEY_INITIAL_ALLOCATION = "key_initial_monthly_allocation"
        private const val KEY_SAVED_YEAR_MONTH = "key_saved_year_month"

        fun getCurrentYearMonth(): Int {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed (0 = Jan)
            return year * 100 + month
        }
    }

    var wasRolledOver: Boolean = false
        private set

    init {
        wasRolledOver = checkAndResetMonthlyRollover()
    }

    fun checkAndResetMonthlyRollover(): Boolean {
        val currentYearMonth = getCurrentYearMonth()
        val savedYearMonth = prefs.getInt(KEY_SAVED_YEAR_MONTH, -1)

        if (savedYearMonth == -1) {
            // First run, initialize current month
            prefs.edit().putInt(KEY_SAVED_YEAR_MONTH, currentYearMonth).apply()
            return false
        } else if (savedYearMonth != currentYearMonth) {
            // New calendar month rolled over! Reset initial allocation to 0.0
            prefs.edit()
                .putFloat(KEY_INITIAL_ALLOCATION, 0f)
                .putInt(KEY_SAVED_YEAR_MONTH, currentYearMonth)
                .apply()
            wasRolledOver = true
            return true
        }
        return false
    }

    fun getInitialMonthlyAllocation(): Double {
        return prefs.getFloat(KEY_INITIAL_ALLOCATION, 0f).toDouble()
    }

    fun setInitialMonthlyAllocation(allocation: Double) {
        wasRolledOver = false
        prefs.edit()
            .putFloat(KEY_INITIAL_ALLOCATION, allocation.toFloat())
            .putInt(KEY_SAVED_YEAR_MONTH, getCurrentYearMonth())
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
