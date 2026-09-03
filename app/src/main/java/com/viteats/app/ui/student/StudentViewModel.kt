package com.viteats.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.remote.BalanceResponse
import com.viteats.app.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BalanceState {
    object Loading : BalanceState()
    data class Success(val balance: BalanceResponse) : BalanceState()
    data class Error(val message: String) : BalanceState()
}

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _balanceState = MutableStateFlow<BalanceState>(BalanceState.Loading)
    val balanceState: StateFlow<BalanceState> = _balanceState

    private val _budgetLimit = MutableStateFlow<Double>(3000.0)
    val budgetLimit: StateFlow<Double> = _budgetLimit

    private val _currentSpending = MutableStateFlow<Double>(2450.0)
    val currentSpending: StateFlow<Double> = _currentSpending

    fun updateBudgetLimit(newLimit: Double) {
        if (newLimit > 0) {
            _budgetLimit.value = newLimit
        }
    }

    fun updateCurrentSpending(spending: Double) {
        if (spending >= 0) {
            _currentSpending.value = spending
        }
    }

    fun fetchBalance() {
        viewModelScope.launch {
            _balanceState.value = BalanceState.Loading
            try {
                val response = repository.getBalance()
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    _balanceState.value = BalanceState.Success(response.body()!![0])
                } else {
                    _balanceState.value = BalanceState.Error("Failed to fetch balance")
                }
            } catch (e: Exception) {
                _balanceState.value = BalanceState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
