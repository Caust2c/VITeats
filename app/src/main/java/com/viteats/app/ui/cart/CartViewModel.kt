package com.viteats.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.model.CartItem
import com.viteats.app.data.remote.MenuItem
import com.viteats.app.data.repository.CartRepository
import com.viteats.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Processing : CheckoutState()
    data class Success(val orderId: String) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

class CartViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    val cartItems: StateFlow<List<CartItem>> = cartRepository.cartItems

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    fun addItem(item: MenuItem) {
        cartRepository.addItem(item)
    }

    fun decrementItem(item: MenuItem) {
        cartRepository.decrementItem(item)
    }

    fun removeItem(item: MenuItem) {
        cartRepository.removeItem(item)
    }

    fun clearCart() {
        cartRepository.clearCart()
    }

    fun checkout(pin: String) {
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Processing
            val items = cartItems.value
            if (items.isEmpty()) {
                _checkoutState.value = CheckoutState.Error("Cart is empty.")
                return@launch
            }

            val result = orderRepository.checkoutOrder(items, pin)
            result.onSuccess { orderId ->
                cartRepository.clearCart()
                _checkoutState.value = CheckoutState.Success(orderId)
            }.onFailure { error ->
                _checkoutState.value = CheckoutState.Error(error.message ?: "Failed to complete order checkout.")
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}
