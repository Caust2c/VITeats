package com.viteats.app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MenuState {
    object Loading : MenuState()
    data class Success(val categories: List<com.viteats.app.data.remote.MenuCategory>, val items: List<com.viteats.app.data.remote.MenuItem>) : MenuState()
    data class Error(val message: String) : MenuState()
}

class MenuViewModel(private val repository: MenuRepository) : ViewModel() {
    private val _menuState = MutableStateFlow<MenuState>(MenuState.Loading)
    val menuState: StateFlow<MenuState> = _menuState

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
