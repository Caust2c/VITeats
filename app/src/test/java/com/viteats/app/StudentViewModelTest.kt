package com.viteats.app

import com.viteats.app.data.BudgetManager
import com.viteats.app.data.remote.BalanceResponse
import com.viteats.app.data.repository.StudentRepository
import com.viteats.app.ui.student.StudentViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy
import android.content.SharedPreferences

class StudentViewModelTest {

    private fun createFakeSharedPreferences(initialValues: Map<String, Any?> = emptyMap()): SharedPreferences {
        val map = HashMap<String, Any?>(initialValues)

        var editorProxy: SharedPreferences.Editor? = null
        editorProxy = Proxy.newProxyInstance(
            SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java)
        ) { _, method, args ->
            when (method.name) {
                "putFloat" -> {
                    map[args[0] as String] = args[1] as Float
                    editorProxy
                }
                "putInt" -> {
                    map[args[0] as String] = args[1] as Int
                    editorProxy
                }
                "apply", "commit" -> null
                else -> editorProxy
            }
        } as SharedPreferences.Editor

        return Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getFloat" -> {
                    val key = args[0] as String
                    val defValue = args[1] as Float
                    (map[key] as? Float) ?: defValue
                }
                "getInt" -> {
                    val key = args[0] as String
                    val defValue = args[1] as Int
                    (map[key] as? Int) ?: defValue
                }
                "edit" -> editorProxy
                else -> null
            }
        } as SharedPreferences
    }

    @Test
    fun testSpendingInitializedAtZero() {
        val viewModel = StudentViewModel(StudentRepository())
        assertEquals(0.0, viewModel.currentSpending.value, 0.001)
        assertEquals(0.0, viewModel.initialMonthlyAllocation.value, 0.001)
    }

    @Test
    fun testSetMonthlyAllocation() {
        val prefs = createFakeSharedPreferences()
        val budgetManager = BudgetManager(prefs)
        val viewModel = StudentViewModel(StudentRepository(), budgetManager)

        viewModel.setInitialMonthlyAllocation(9000.0)
        assertEquals(9000.0, viewModel.initialMonthlyAllocation.value, 0.001)
        assertEquals(9000.0, budgetManager.getInitialMonthlyAllocation(), 0.001)

        // Negative values should be ignored
        viewModel.setInitialMonthlyAllocation(-500.0)
        assertEquals(9000.0, viewModel.initialMonthlyAllocation.value, 0.001)
    }

    @Test
    fun testDynamicSpendingFormula() {
        val prefs = createFakeSharedPreferences()
        val budgetManager = BudgetManager(prefs)
        val viewModel = StudentViewModel(StudentRepository(), budgetManager)

        // Set initial monthly allocation to ₹9000
        viewModel.setInitialMonthlyAllocation(9000.0)

        // Current wallet balance is ₹7750
        // formula: currentSpending = initialMonthlyAllocation - currentWalletBalance = 9000 - 7750 = 1250
        val walletBalance = 7750.0
        val expectedSpending = (viewModel.initialMonthlyAllocation.value - walletBalance).coerceAtLeast(0.0)
        viewModel.updateCurrentSpending(expectedSpending)

        assertEquals(1250.0, viewModel.currentSpending.value, 0.001)
    }

    @Test
    fun testMonthlyRolloverReset() {
        // Saved month was previous month e.g. 202608 (August)
        val pastMonth = 202608
        val prefs = createFakeSharedPreferences(
            mapOf(
                "key_saved_year_month" to pastMonth,
                "key_initial_monthly_allocation" to 9000f
            )
        )
        val budgetManager = BudgetManager(prefs)
        val viewModel = StudentViewModel(StudentRepository(), budgetManager)

        // Since current month is 202609, rollover occurred!
        assertTrue(viewModel.isMonthRolledOver.value)
        assertEquals(0.0, viewModel.initialMonthlyAllocation.value, 0.001)
        assertEquals(0.0, viewModel.currentSpending.value, 0.001)
    }
}
