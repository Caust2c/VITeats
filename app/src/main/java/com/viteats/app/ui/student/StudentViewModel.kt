package com.viteats.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.BudgetManager
import com.viteats.app.data.remote.BalanceResponse
import com.viteats.app.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BalanceState {
    object Loading : BalanceState()
    data class Success(val balance: BalanceResponse) : BalanceState()
    data class Error(val message: String) : BalanceState()
}

class StudentViewModel(
    private val repository: StudentRepository,
    private val budgetManager: BudgetManager? = null
) : ViewModel() {
    private val _balanceState = MutableStateFlow<BalanceState>(BalanceState.Loading)
    val balanceState: StateFlow<BalanceState> = _balanceState

    private val _initialMonthlyAllocation = MutableStateFlow<Double>(
        budgetManager?.getInitialMonthlyAllocation() ?: 0.0
    )
    val initialMonthlyAllocation: StateFlow<Double> = _initialMonthlyAllocation.asStateFlow()

    // Backwards-compatible alias for existing tests
    val budgetLimit: StateFlow<Double> = _initialMonthlyAllocation

    private val _currentSpending = MutableStateFlow<Double>(0.0)
    val currentSpending: StateFlow<Double> = _currentSpending.asStateFlow()

    private val _isMonthRolledOver = MutableStateFlow<Boolean>(false)
    val isMonthRolledOver: StateFlow<Boolean> = _isMonthRolledOver.asStateFlow()

    init {
        checkMonthlyReset()
    }

    fun checkMonthlyReset() {
        val didReset = budgetManager?.wasRolledOver == true || (budgetManager?.checkAndResetMonthlyRollover() ?: false)
        if (didReset) {
            _initialMonthlyAllocation.value = 0.0
            _currentSpending.value = 0.0
            _isMonthRolledOver.value = true
        } else {
            _initialMonthlyAllocation.value = budgetManager?.getInitialMonthlyAllocation() ?: _initialMonthlyAllocation.value
            recalculateSpending()
        }
    }

    fun setInitialMonthlyAllocation(allocation: Double) {
        if (allocation >= 0) {
            _initialMonthlyAllocation.value = allocation
            budgetManager?.setInitialMonthlyAllocation(allocation)
            _isMonthRolledOver.value = false
            recalculateSpending()
        }
    }

    fun updateBudgetLimit(newLimit: Double) {
        setInitialMonthlyAllocation(newLimit)
    }

    fun updateCurrentSpending(spending: Double) {
        if (spending >= 0) {
            _currentSpending.value = spending
        }
    }

    fun recalculateSpending() {
        val currentBal = (_balanceState.value as? BalanceState.Success)?.balance?.bal ?: return
        val allocation = _initialMonthlyAllocation.value
        if (allocation > 0) {
            val spent = (allocation - currentBal).coerceAtLeast(0.0)
            _currentSpending.value = spent
        } else {
            _currentSpending.value = 0.0
        }
    }

    fun fetchBalance() {
        viewModelScope.launch {
            _balanceState.value = BalanceState.Loading
            try {
                val response = repository.getBalance()
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val balance = response.body()!![0]
                    _balanceState.value = BalanceState.Success(balance)
                    recalculateSpending()
                } else {
                    _balanceState.value = BalanceState.Error("Failed to fetch balance")
                }
            } catch (e: Exception) {
                _balanceState.value = BalanceState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
