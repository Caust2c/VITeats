package com.viteats.app

import com.viteats.app.data.repository.StudentRepository
import com.viteats.app.ui.student.StudentViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentViewModelTest {

    @Test
    fun testDefaultBudgetLimitAndSpending() {
        val viewModel = StudentViewModel(StudentRepository())
        assertEquals(3000.0, viewModel.budgetLimit.value, 0.001)
        assertEquals(2450.0, viewModel.currentSpending.value, 0.001)

        // Spending / limit >= 0.8
        val ratio = viewModel.currentSpending.value / viewModel.budgetLimit.value
        assertTrue(ratio >= 0.8)
    }

    @Test
    fun testUpdateBudgetLimit() {
        val viewModel = StudentViewModel(StudentRepository())
        viewModel.updateBudgetLimit(5000.0)
        assertEquals(5000.0, viewModel.budgetLimit.value, 0.001)

        // Invalid budget limit <= 0 should be ignored
        viewModel.updateBudgetLimit(-100.0)
        assertEquals(5000.0, viewModel.budgetLimit.value, 0.001)
    }

    @Test
    fun testUpdateCurrentSpending() {
        val viewModel = StudentViewModel(StudentRepository())
        viewModel.updateCurrentSpending(1200.0)
        assertEquals(1200.0, viewModel.currentSpending.value, 0.001)

        // Ratio with 1200 / 3000 = 0.4 < 0.8
        val ratio = viewModel.currentSpending.value / viewModel.budgetLimit.value
        assertTrue(ratio < 0.8)
    }
}
