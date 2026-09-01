package com.viteats.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.viteats.app.VITeatsApplication
import com.viteats.app.ui.auth.AuthViewModel
import com.viteats.app.ui.cart.CartViewModel
import com.viteats.app.ui.menu.MenuViewModel
import com.viteats.app.ui.orders.OrdersViewModel
import com.viteats.app.ui.student.StudentViewModel

class ViewModelFactory(private val application: VITeatsApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> 
                AuthViewModel(application.authRepository) as T
            modelClass.isAssignableFrom(StudentViewModel::class.java) ->
                StudentViewModel(application.studentRepository) as T
            modelClass.isAssignableFrom(MenuViewModel::class.java) ->
                MenuViewModel(application.menuRepository, application.cartRepository, application.favouritesManager) as T
            modelClass.isAssignableFrom(OrdersViewModel::class.java) ->
                OrdersViewModel(application.orderRepository, application.cartRepository, application.menuRepository) as T
            modelClass.isAssignableFrom(CartViewModel::class.java) ->
                CartViewModel(application.cartRepository, application.orderRepository) as T
            modelClass.isAssignableFrom(com.viteats.app.ui.settings.SettingsViewModel::class.java) ->
                com.viteats.app.ui.settings.SettingsViewModel(application.themeManager, application.authRepository, application.studentRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
