package com.viteats.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viteats.app.data.remote.Order
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.components.NeobrutalPill
import com.viteats.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(viewModel: OrdersViewModel, onOrderClick: (String) -> Unit) {
    val ordersState by viewModel.ordersState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LavenderBackground)
    ) {
        when (val state = ordersState) {
            is OrdersState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeobrutalBlack)
                }
            }
            is OrdersState.Success -> {
                if (state.orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        NeobrutalCard(
                            backgroundColor = NeobrutalWhite,
                            shadowOffset = 5.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = NeobrutalBlack
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Past Orders Found",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = NeobrutalBlack
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your completed and upcoming mess orders will appear here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedText
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                NeobrutalButton(
                                    onClick = { viewModel.fetchOrders() },
                                    backgroundColor = PastelYellow
                                ) {
                                    Text("Refresh Orders", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.orders) { order ->
                            OrderListItem(
                                order = order,
                                onClick = {
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
                    NeobrutalCard(
                        backgroundColor = SoftCoral,
                        shadowOffset = 4.dp,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = NeobrutalBlack,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            NeobrutalButton(
                                onClick = { viewModel.fetchOrders() },
                                backgroundColor = NeobrutalWhite
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
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

    NeobrutalCard(
        backgroundColor = NeobrutalWhite,
        borderColor = NeobrutalBlack,
        borderWidth = 2.dp,
        shadowOffset = 4.dp,
        cornerRadius = 16.dp,
        onClick = if (qrAvailable) onClick else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = order.OrderDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = order.sname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = NeobrutalBlack
                )
                if (qrAvailable) {
                    Text(
                        text = "Tap to view QR",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₹${order.NetAmount.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NeobrutalBlack
                )
                Spacer(modifier = Modifier.height(6.dp))
                NeobrutalStatusBadge(status = order.Status)
            }
        }
    }
}

@Composable
fun NeobrutalStatusBadge(status: String) {
    val isDelivered = status.equals("Delivered", ignoreCase = true) || status.equals("Success", ignoreCase = true)
    val isPending = status.equals("Pending", ignoreCase = true) || status.equals("Placed", ignoreCase = true)
    val bg = when {
        isDelivered -> MintGreen
        isPending -> PastelYellow
        else -> SoftCoral
    }

    NeobrutalPill(
        text = status,
        backgroundColor = bg,
        textColor = NeobrutalBlack,
        isSelected = false
    )
}

fun isQrAvailable(order: Order): Boolean {
    if (order.Status == "Delivered" || order.Status == "Success") return false
    if (order.Status.lowercase().contains("fail") || order.CancelStatus == "Cancel") return false

    return try {
        val sdf = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
        val orderDate = sdf.parse(order.OrderDate) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        !orderDate.before(today)
    } catch (e: Exception) {
        false
    }
}

