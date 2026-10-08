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

    @Test
    fun testCategoryAggregationFromOrders() {
        val viewModel = StudentViewModel(StudentRepository())

        val testOrders = listOf(
            com.viteats.app.data.remote.Order(
                OrderDate = "13/SEP/2026",
                OrderTime = "08:30 AM",
                NetAmount = 100.0,
                OrderId = "101",
                Status = "Success",
                CancelStatus = "Cancel",
                sname = "BREAKFAST",
                qrstat = 1,
                RegNo = "21BCE1234",
                studname = "Test Student"
            ),
            com.viteats.app.data.remote.Order(
                OrderDate = "13/SEP/2026",
                OrderTime = "01:15 PM",
                NetAmount = 200.0,
                OrderId = "102",
                Status = "Delivered",
                CancelStatus = "Cancel",
                sname = "Special Lunch Thali",
                qrstat = 1,
                RegNo = "21BCE1234",
                studname = "Test Student"
            ),
            com.viteats.app.data.remote.Order(
                OrderDate = "13/SEP/2026",
                OrderTime = "05:00 PM",
                NetAmount = 50.0,
                OrderId = "103",
                Status = "Success",
                CancelStatus = "Cancel",
                sname = "Samosa and Tea",
                qrstat = 1,
                RegNo = "21BCE1234",
                studname = "Test Student"
            ),
            com.viteats.app.data.remote.Order(
                OrderDate = "13/SEP/2026",
                OrderTime = "08:00 PM",
                NetAmount = 150.0,
                OrderId = "104",
                Status = "Success",
                CancelStatus = "Cancel",
                sname = "Dinner Buffet",
                qrstat = 1,
                RegNo = "21BCE1234",
                studname = "Test Student"
            ),
            com.viteats.app.data.remote.Order(
                OrderDate = "13/SEP/2026",
                OrderTime = "01:00 PM",
                NetAmount = 300.0,
                OrderId = "105",
                Status = "Cancelled",
                CancelStatus = "Cancelled",
                sname = "LUNCH",
                qrstat = 0,
                RegNo = "21BCE1234",
                studname = "Test Student"
            )
        )

        viewModel.processOrdersForExpenses(testOrders)

        val spending = viewModel.categorySpending.value
        assertEquals(100.0, spending.breakfast, 0.001)
        assertEquals(200.0, spending.lunch, 0.001)
        assertEquals(50.0, spending.snacks, 0.001)
        assertEquals(150.0, spending.dinner, 0.001)
        assertEquals(500.0, spending.totalSpent, 0.001)

        // Percentages:
        // Lunch: 200 / 500 = 40%
        // Dinner: 150 / 500 = 30%
        // Breakfast: 100 / 500 = 20%
        // Snacks: 50 / 500 = 10%
        assertEquals(40, spending.lunchPercentage)
        assertEquals(30, spending.dinnerPercentage)
        assertEquals(20, spending.breakfastPercentage)
        assertEquals(10, spending.snacksPercentage)
    }

    @Test
    fun testOrderClassificationByTimeFallback() {
        val viewModel = StudentViewModel(StudentRepository())

        val orderBreakfast = com.viteats.app.data.remote.Order(
            OrderDate = "13/SEP/2026",
            OrderTime = "09:15 am",
            NetAmount = 80.0,
            OrderId = "201",
            Status = "Success",
            CancelStatus = "Cancel",
            sname = "Mess Counter",
            qrstat = 1,
            RegNo = "21BCE1234",
            studname = "Test Student"
        )
        assertEquals("Breakfast", viewModel.classifyOrderCategory(orderBreakfast))

        val orderLunch = com.viteats.app.data.remote.Order(
            OrderDate = "13/SEP/2026",
            OrderTime = "12:30 pm",
            NetAmount = 120.0,
            OrderId = "202",
            Status = "Success",
            CancelStatus = "Cancel",
            sname = "Mess Counter",
            qrstat = 1,
            RegNo = "21BCE1234",
            studname = "Test Student"
        )
        assertEquals("Lunch", viewModel.classifyOrderCategory(orderLunch))

        val orderSnacks = com.viteats.app.data.remote.Order(
            OrderDate = "13/SEP/2026",
            OrderTime = "05:30 pm",
            NetAmount = 40.0,
            OrderId = "203",
            Status = "Success",
            CancelStatus = "Cancel",
            sname = "Mess Counter",
            qrstat = 1,
            RegNo = "21BCE1234",
            studname = "Test Student"
        )
        assertEquals("Snacks", viewModel.classifyOrderCategory(orderSnacks))

        val orderDinner = com.viteats.app.data.remote.Order(
            OrderDate = "13/SEP/2026",
            OrderTime = "08:15 pm",
            NetAmount = 130.0,
            OrderId = "204",
            Status = "Success",
            CancelStatus = "Cancel",
            sname = "Mess Counter",
            qrstat = 1,
            RegNo = "21BCE1234",
            studname = "Test Student"
        )
        assertEquals("Dinner", viewModel.classifyOrderCategory(orderDinner))
    }
}
