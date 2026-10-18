package com.viteats.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.remote.Order
import com.viteats.app.data.repository.CartRepository
import com.viteats.app.data.repository.MenuRepository
import com.viteats.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

class OrdersViewModel(
    private val repository: OrderRepository,
    private val cartRepository: CartRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {
    private val _ordersState = MutableStateFlow<OrdersState>(OrdersState.Loading)
    val ordersState: StateFlow<OrdersState> = _ordersState

    private val _qrState = MutableStateFlow<OrderQRState>(OrderQRState.Idle)
    val qrState: StateFlow<OrderQRState> = _qrState

    private val _reorderMessage = MutableSharedFlow<String>()
    val reorderMessage: SharedFlow<String> = _reorderMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeOrders().collect { orders ->
                if (orders.isNotEmpty()) _ordersState.value = OrdersState.Success(orders, "")
            }
        }
        viewModelScope.launch { repository.ordersUpdated.collect { fetchOrders() } }
    }

    fun fetchOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            try {
                val cached = repository.cachedOrders()
                if (cached.isNotEmpty()) _ordersState.value = OrdersState.Success(cached, "")
                val orders = repository.getOrders()
                _ordersState.value = OrdersState.Success(orders, "")
            } catch (e: Exception) {
                val cached = repository.cachedOrders()
                _ordersState.value = if (cached.isNotEmpty()) OrdersState.Success(cached, "")
                    else OrdersState.Error(e.message ?: "Unknown error")
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

    fun reorderPastOrder(order: Order, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val currentMenuItems = menuRepository.getMenuItems()
                val saved = repository.savedItems(order.OrderId)
                val items = com.viteats.app.data.OrderingPolicy.reorder(saved, currentMenuItems)
                // Validate the merged cart before changing anything.
                val merged = cartRepository.cartItems.value.toMutableList()
                items.forEach { line ->
                    val index = merged.indexOfFirst { it.item.meitid == line.item.meitid }
                    if (index >= 0) merged[index] = line.copy(quantity = merged[index].quantity + line.quantity)
                    else merged.add(line)
                }
                com.viteats.app.data.OrderingPolicy.reorder(merged, currentMenuItems)
                cartRepository.addItems(items.map { it.item to it.quantity })
                _reorderMessage.emit("Added ${items.sumOf { it.quantity }} items to your cart at current prices.")
                onComplete?.invoke()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _reorderMessage.emit(e.message ?: "Unable to reorder. Cart was not changed.")
            }
        }
    }
}

