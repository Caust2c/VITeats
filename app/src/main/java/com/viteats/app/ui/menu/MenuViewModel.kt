package com.viteats.app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.model.CartItem
import com.viteats.app.data.remote.MenuCategory
import com.viteats.app.data.remote.MenuItem
import com.viteats.app.data.repository.CartRepository
import com.viteats.app.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class MenuState {
    object Loading : MenuState()
    data class Success(val categories: List<MenuCategory>, val items: List<MenuItem>) : MenuState()
    data class Error(val message: String) : MenuState()
}

class MenuViewModel(
    private val repository: MenuRepository,
    private val cartRepository: CartRepository,
    private val favouritesManager: com.viteats.app.data.FavouritesManager
) : ViewModel() {
    private val _menuState = MutableStateFlow<MenuState>(MenuState.Loading)
    val menuState: StateFlow<MenuState> = _menuState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val cartItems: StateFlow<List<CartItem>> = cartRepository.cartItems

    val favouriteItemIds: StateFlow<Set<Int>> = favouritesManager.favouriteItemIds

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavourite(item: MenuItem) {
        favouritesManager.toggleFavourite(item.meitid)
    }

    fun isFavourite(item: MenuItem): Boolean {
        return favouritesManager.isFavourite(item.meitid)
    }

    fun addToCart(item: MenuItem) {
        cartRepository.addItem(item)
    }

    fun decrementItem(item: MenuItem) {
        cartRepository.decrementItem(item)
    }

    fun removeFromCart(item: MenuItem) {
        cartRepository.removeItem(item)
    }

    fun fetchMenu() {
        viewModelScope.launch {
            _menuState.value = MenuState.Loading
            try {
                val categories = repository.getCategories()
                val items = repository.getMenuItems()
                
                if (categories.isEmpty() && items.isEmpty()) {
                    _menuState.value = MenuState.Error("Mess is currently closed or no menu items available.")
                } else {
                    _menuState.value = MenuState.Success(categories, items)
                }
            } catch (e: Exception) {
                _menuState.value = MenuState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

