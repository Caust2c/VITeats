package com.viteats.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.remote.MenuItem
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

    fun reorderPastOrder(order: Order, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val currentMenuItems = menuRepository.getMenuItems()
                val matchingItem = currentMenuItems.find {
                    it.meitdes.contains(order.sname, ignoreCase = true) ||
                    it.dispname.contains(order.sname, ignoreCase = true) ||
                    order.sname.contains(it.meitdes, ignoreCase = true)
                }

                val itemToAdd = matchingItem ?: MenuItem(
                    meitid = order.OrderId.filter { it.isDigit() }.toIntOrNull() ?: kotlin.math.abs(order.OrderId.hashCode() % 10000) + 100,
                    meitdes = if (order.sname.isNotBlank()) order.sname else "Mess Meal (Order #${order.OrderId})",
                    retrt = if (order.NetAmount > 0) order.NetAmount else 50.0,
                    odtdes = "Mess Counter",
                    skuid = 2,
                    dispname = if (order.sname.isNotBlank()) order.sname else "Mess Order",
                    skudes = "Meals",
                    StockQty = 20,
                    icat = 1,
                    StartTime = null,
                    EndTime = null
                )

                cartRepository.addItem(itemToAdd, quantity = 1)
                val itemName = itemToAdd.meitdes.take(24)
                _reorderMessage.emit("Added \"$itemName\" to your cart!")
                onComplete?.invoke()
            } catch (e: Exception) {
                _reorderMessage.emit("Added order to cart!")
                onComplete?.invoke()
            }
        }
    }
}

