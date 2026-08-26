package com.viteats.app.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.viteats.app.data.remote.Order
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(viewModel: OrdersViewModel, onOrderClick: (String) -> Unit) {
    val ordersState by viewModel.ordersState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = ordersState) {
            is OrdersState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is OrdersState.Success -> {
                if (state.orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text("No orders found")
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Raw API Response:",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = state.rawResponse,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(onClick = { viewModel.fetchOrders() }) {
                                Text("Refresh")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.orders) { order ->
                            OrderListItem(
                                order = order,
                                onClick = {
                                    // Only allow clicking if QR might be available
                                    if (isQrAvailable(order)) {
                                        onOrderClick(order.OrderId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            is OrdersState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.fetchOrders() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderListItem(order: Order, onClick: () -> Unit) {
    val qrAvailable = isQrAvailable(order)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = qrAvailable) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.OrderDate,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = order.sname,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column(modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "₹ ${order.NetAmount.toInt()}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                StatusBadge(status = order.Status)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Delivered", "Success" -> Color(0xFF6366F1) // Purpleish blue from screenshot
        else -> Color.Gray
    }
    
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

fun isQrAvailable(order: Order): Boolean {
    // 1. If delivered or success, QR is gone
    if (order.Status == "Delivered" || order.Status == "Success") return false
    
    // 2. If status is failed or cancelled, no QR
    if (order.Status.lowercase().contains("fail") || order.CancelStatus == "Cancel") return false

    // 3. Check if session expired (Old date)
    return try {
        val sdf = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
        val orderDate = sdf.parse(order.OrderDate) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        // QR only available on the same day
        !orderDate.before(today)
    } catch (e: Exception) {
        false
    }
}
