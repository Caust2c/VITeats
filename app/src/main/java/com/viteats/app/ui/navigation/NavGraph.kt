package com.viteats.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.viteats.app.VITeatsApplication
import com.viteats.app.ui.ViewModelFactory
import com.viteats.app.ui.auth.AuthScreen
import com.viteats.app.ui.cart.CartScreen
import com.viteats.app.ui.cart.CartViewModel
import com.viteats.app.ui.home.HomeScreen
import com.viteats.app.ui.orders.OrderDetailsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Cart : Screen("cart")
    object OrderDetails : Screen("order_details/{orderId}") {
        fun createRoute(orderId: String) = "order_details/$orderId"
    }
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    val context = LocalContext.current
    val app = context.applicationContext as VITeatsApplication

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            AuthScreen(onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetails.createRoute(orderId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }
        composable(Screen.Cart.route) {
            val cartViewModel: CartViewModel = viewModel(factory = ViewModelFactory(app))
            CartScreen(
                viewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onCheckoutSuccess = { orderId ->
                    navController.navigate(Screen.OrderDetails.createRoute(orderId)) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.OrderDetails.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailsScreen(orderId = orderId, onBack = {
                navController.popBackStack()
            })
        }
    }
}

