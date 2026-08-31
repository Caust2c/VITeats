package com.viteats.app.data.repository

import com.viteats.app.data.model.CartItem
import com.viteats.app.data.remote.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addItem(item: MenuItem) {
        _cartItems.update { currentList ->
            val existing = currentList.find { it.item.meitid == item.meitid }
            if (existing != null) {
                currentList.map {
                    if (it.item.meitid == item.meitid) it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                currentList + CartItem(item = item, quantity = 1)
            }
        }
    }

    fun decrementItem(item: MenuItem) {
        _cartItems.update { currentList ->
            val existing = currentList.find { it.item.meitid == item.meitid }
            if (existing != null) {
                if (existing.quantity > 1) {
                    currentList.map {
                        if (it.item.meitid == item.meitid) it.copy(quantity = it.quantity - 1)
                        else it
                    }
                } else {
                    currentList.filter { it.item.meitid != item.meitid }
                }
            } else {
                currentList
            }
        }
    }

    fun removeItem(item: MenuItem) {
        _cartItems.update { currentList ->
            currentList.filter { it.item.meitid != item.meitid }
        }
    }

    fun getItemQuantity(meitid: Int): Int {
        return _cartItems.value.find { it.item.meitid == meitid }?.quantity ?: 0
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    val totalAmount: Double
        get() = _cartItems.value.sumOf { it.lineTotal }

    val totalItemCount: Int
        get() = _cartItems.value.sumOf { it.quantity }
}
