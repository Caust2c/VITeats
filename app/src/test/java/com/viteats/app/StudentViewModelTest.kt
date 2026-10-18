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
import java.util.Calendar
import java.util.TimeZone

class StudentViewModelTest {
    private fun fixedCalendar(): Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
        clear()
        set(2026, Calendar.SEPTEMBER, 20, 12, 0)
    }

    private fun createViewModel(budgetManager: BudgetManager? = null) =
        StudentViewModel(StudentRepository(), budgetManager, calendarProvider = ::fixedCalendar)

    private fun order(date: String = "13/SEP/2026", status: String = "Success", amount: Double = 100.0, id: String = "1") =
        com.viteats.app.data.remote.Order(date, "12:30 pm", amount, id, status, "Cancel", "LUNCH", 1, "TEST", "Test")

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
                "putString" -> { map[args[0] as String] = args[1]; editorProxy }
                "remove" -> { map.remove(args[0] as String); editorProxy }
                "clear" -> { map.clear(); editorProxy }
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
                "getString" -> map[args[0] as String] as? String ?: args[1]
                else -> null
            }
        } as SharedPreferences
    }

    @Test
    fun testSpendingInitializedAtZero() {
        val viewModel = createViewModel()
        assertEquals(0.0, viewModel.currentSpending.value, 0.001)
        assertEquals(0.0, viewModel.initialMonthlyAllocation.value, 0.001)
    }

    @Test
    fun testSetMonthlyAllocation() {
        val prefs = createFakeSharedPreferences()
        val budgetManager = BudgetManager(prefs)
        val viewModel = createViewModel(budgetManager)

        viewModel.setInitialMonthlyAllocation(9000.0)
        assertEquals(9000.0, viewModel.initialMonthlyAllocation.value, 0.001)
        assertEquals(9000.0, budgetManager.getInitialMonthlyAllocation(), 0.001)

        // Negative values should be ignored
        viewModel.setInitialMonthlyAllocation(-500.0)
        assertEquals(9000.0, viewModel.initialMonthlyAllocation.value, 0.001)
    }

    @Test
    fun testBudgetEditsDoNotChangeActualOrderSpending() {
        val prefs = createFakeSharedPreferences()
        val budgetManager = BudgetManager(prefs)
        val viewModel = createViewModel(budgetManager)

        // Set initial monthly allocation to ₹9000
        viewModel.setInitialMonthlyAllocation(9000.0)

        viewModel.processOrdersForExpenses(listOf(order(amount = 1250.0)))
        viewModel.setInitialMonthlyAllocation(8000.50)
        viewModel.recalculateSpending()
        assertEquals(1250.0, viewModel.currentSpending.value, 0.001)
        assertEquals(8000.50, budgetManager.getInitialMonthlyAllocation(), 0.001)
    }

    @Test
    fun testMonthlyRolloverReset() {
        // Saved month was previous month e.g. 202608 (August)
        val current = BudgetManager.getCurrentYearMonth()
        val pastMonth = if (current % 100 == 1) (current / 100 - 1) * 100 + 12 else current - 1
        val prefs = createFakeSharedPreferences(
            mapOf(
                "key_saved_year_month" to pastMonth,
                "key_initial_monthly_allocation" to 9000f
            )
        )
        val budgetManager = BudgetManager(prefs)
        val viewModel = createViewModel(budgetManager)

        // Since current month is 202609, rollover occurred!
        assertTrue(viewModel.isMonthRolledOver.value)
        assertEquals(0.0, viewModel.initialMonthlyAllocation.value, 0.001)
        assertEquals(0.0, viewModel.currentSpending.value, 0.001)
    }

    @Test
    fun testCategoryAggregationFromOrders() {
        val viewModel = createViewModel()

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
        val viewModel = createViewModel()

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

    @Test
    fun emptyMonthNeverFallsBackToOlderOrders() {
        val vm = createViewModel()
        vm.processOrdersForExpenses(listOf(order(date = "13/AUG/2026")))
        assertEquals(0.0, vm.currentSpending.value, 0.001)
        assertEquals(0.0, vm.categorySpending.value.totalSpent, 0.001)
    }

    @Test
    fun unsuccessfulAndDuplicateOrdersDoNotInflateSpending() {
        val vm = createViewModel()
        val paid = order(amount = 120.50)
        vm.processOrdersForExpenses(listOf(paid, paid, order(status = "Pending", id = "2"),
            order(status = "Failed", id = "3"), order(status = "Cancelled", id = "4")))
        assertEquals(120.50, vm.currentSpending.value, 0.001)
        vm.processOrdersForExpenses(emptyList())
        assertEquals(0.0, vm.currentSpending.value, 0.001)
    }

    @Test
    fun datesAreStrictAndUseTheFullMonthAndYear() {
        val vm = createViewModel()
        assertTrue(vm.isOrderInCurrentMonth("2026-09-13"))
        assertTrue(vm.isOrderInCurrentMonth("13-SEP-2026"))
        listOf("", "31/SEP/2026", "13/SEP/2026 garbage", "13/SEP/2025", "13/OCT/2026", "30/SEP/2026").forEach {
            assertFalse(it, vm.isOrderInCurrentMonth(it))
        }
    }

    @Test
    fun nonFiniteLimitsAreRejected() {
        val vm = createViewModel()
        vm.setInitialMonthlyAllocation(1000.0)
        vm.setInitialMonthlyAllocation(Double.POSITIVE_INFINITY)
        vm.setInitialMonthlyAllocation(Double.NaN)
        assertEquals(1000.0, vm.initialMonthlyAllocation.value, 0.001)
    }
}
