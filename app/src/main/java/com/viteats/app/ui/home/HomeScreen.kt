package com.viteats.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viteats.app.VITeatsApplication
import com.viteats.app.ui.ViewModelFactory
import com.viteats.app.ui.auth.AuthViewModel
import com.viteats.app.ui.menu.MenuScreen
import com.viteats.app.ui.menu.MenuViewModel
import com.viteats.app.ui.orders.OrdersScreen
import com.viteats.app.ui.orders.OrdersViewModel
import com.viteats.app.ui.student.StudentScreen
import com.viteats.app.ui.student.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOrderClick: (String) -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication)),
    studentViewModel: StudentViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication)),
    menuViewModel: MenuViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication)),
    ordersViewModel: OrdersViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication))
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VITeats") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                    label = { Text("Menu") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> StudentScreen(viewModel = studentViewModel)
                1 -> MenuScreen(viewModel = menuViewModel)
                2 -> OrdersScreen(viewModel = ordersViewModel, onOrderClick = onOrderClick)
            }
        }
    }

    // Refresh data when switching tabs or initially
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> studentViewModel.fetchBalance()
            1 -> menuViewModel.fetchMenu()
            2 -> ordersViewModel.fetchOrders()
        }
    }
}
