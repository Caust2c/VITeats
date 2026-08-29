package com.viteats.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.remote.Order
import com.viteats.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OrdersState {
    object Loading : OrdersState()
    data class Success(val orders: List<Order>, val rawResponse: String) : OrdersState()
    data class Error(val message: String) : OrdersState()
}

sealed class OrderQRState {
    object Idle : OrderQRState()
    object Loading : OrderQRState()
    data class Success(val qrData: String) : OrderQRState()
    data class Error(val message: String) : OrderQRState()
}

class OrdersViewModel(private val repository: OrderRepository) : ViewModel() {
    private val _ordersState = MutableStateFlow<OrdersState>(OrdersState.Loading)
    val ordersState: StateFlow<OrdersState> = _ordersState

    private val _qrState = MutableStateFlow<OrderQRState>(OrderQRState.Idle)
    val qrState: StateFlow<OrderQRState> = _qrState

    fun fetchOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            try {
                val rawResponse = repository.getRawOrders()
                val orders = repository.parseOrders(rawResponse)
                _ordersState.value = OrdersState.Success(orders, rawResponse)
            } catch (e: Exception) {
                _ordersState.value = OrdersState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchQR(orderId: String) {
        viewModelScope.launch {
            _qrState.value = OrderQRState.Loading
            try {
                val qrData = repository.getOrderQR(orderId)
                if (qrData != null) {
                    _qrState.value = OrderQRState.Success(qrData)
                } else {
                    _qrState.value = OrderQRState.Error("Failed to fetch QR. It might have expired or been claimed.")
                }
            } catch (e: Exception) {
                _qrState.value = OrderQRState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearQR() {
        _qrState.value = OrderQRState.Idle
    }
}
