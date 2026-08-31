package com.viteats.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viteats.app.VITeatsApplication
import com.viteats.app.data.SessionEvent
import com.viteats.app.ui.ViewModelFactory
import com.viteats.app.ui.auth.AuthViewModel
import com.viteats.app.ui.menu.MenuScreen
import com.viteats.app.ui.menu.MenuViewModel
import com.viteats.app.ui.orders.OrdersScreen
import com.viteats.app.ui.orders.OrdersViewModel
import com.viteats.app.ui.student.StudentScreen
import com.viteats.app.ui.student.StudentViewModel
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOrderClick: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToCart: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication)),
    studentViewModel: StudentViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication)),
    menuViewModel: MenuViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication)),
    ordersViewModel: OrdersViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as VITeatsApplication))
) {
    val context = LocalContext.current
    val app = context.applicationContext as VITeatsApplication
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        app.sessionManager.sessionEvents.collectLatest { event ->
            if (event is SessionEvent.SessionExpired || event is SessionEvent.LoggedOut) {
                onLogout()
            }
        }
    }

    Scaffold(
        containerColor = LavenderBackground,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = NeobrutalBlack,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 3f
                        )
                    },
                color = NeobrutalWhite
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VITeats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = NeobrutalBlack,
                        letterSpacing = (-0.5).sp
                    )

                    NeobrutalButton(
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        },
                        backgroundColor = SoftCoral,
                        contentColor = NeobrutalBlack,
                        borderColor = NeobrutalBlack,
                        borderWidth = 1.5.dp,
                        shadowOffset = 2.dp,
                        cornerRadius = 10.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(16.dp),
                            tint = NeobrutalBlack
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Logout",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = NeobrutalBlack,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 4f
                        )
                    },
                color = NeobrutalWhite
            ) {
                NavigationBar(
                    containerColor = NeobrutalWhite,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(24.dp)) },
                        label = { Text("Home", fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Bold) },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeobrutalBlack,
                            selectedTextColor = NeobrutalBlack,
                            indicatorColor = MintGreen,
                            unselectedIconColor = NeobrutalBlack.copy(alpha = 0.7f),
                            unselectedTextColor = NeobrutalBlack.copy(alpha = 0.7f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu", modifier = Modifier.size(24.dp)) },
                        label = { Text("Menu", fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Bold) },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeobrutalBlack,
                            selectedTextColor = NeobrutalBlack,
                            indicatorColor = PastelYellow,
                            unselectedIconColor = NeobrutalBlack.copy(alpha = 0.7f),
                            unselectedTextColor = NeobrutalBlack.copy(alpha = 0.7f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "Orders", modifier = Modifier.size(24.dp)) },
                        label = { Text("Orders", fontWeight = if (selectedTab == 2) FontWeight.Black else FontWeight.Bold) },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeobrutalBlack,
                            selectedTextColor = NeobrutalBlack,
                            indicatorColor = SoftCoral,
                            unselectedIconColor = NeobrutalBlack.copy(alpha = 0.7f),
                            unselectedTextColor = NeobrutalBlack.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> StudentScreen(viewModel = studentViewModel, onNavigateToTab = { selectedTab = it })
                1 -> MenuScreen(viewModel = menuViewModel, onNavigateToCart = onNavigateToCart)
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
