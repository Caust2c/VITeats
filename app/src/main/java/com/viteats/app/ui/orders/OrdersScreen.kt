package com.viteats.app.ui.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viteats.app.data.remote.Order
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.components.NeobrutalPill
import com.viteats.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OrdersScreen(viewModel: OrdersViewModel, onOrderClick: (String) -> Unit, onCartReady: () -> Unit = {}) {
    val ordersState by viewModel.ordersState.collectAsState()
    var reorderToastMessage by remember { mutableStateOf<String?>(null) }
    val isDark = LocalDarkTheme.current

    val screenBg = if (isDark) DarkCharcoalBg else LavenderBackground
    val cardBg = if (isDark) DarkCardBg else NeobrutalWhite
    val textPrimary = if (isDark) DarkTextPrimary else NeobrutalBlack
    val textMuted = if (isDark) DarkTextSecondary else MutedText

    LaunchedEffect(Unit) {
        viewModel.reorderMessage.collectLatest { msg ->
            reorderToastMessage = msg
            delay(2500)
            reorderToastMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (val state = ordersState) {
                is OrdersState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = if (isDark) PastelYellow else NeobrutalBlack)
                    }
                }
                is OrdersState.Success -> {
                    if (state.orders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            NeobrutalCard(
                                backgroundColor = cardBg,
                                shadowOffset = 5.dp
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No Past Orders Found",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Your completed and upcoming mess orders will appear here.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textMuted
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
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section Header: Previous Orders
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Previous Orders",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary,
                                        letterSpacing = (-0.5).sp
                                    )

                                    NeobrutalPill(
                                        text = "${state.orders.size} Orders",
                                        backgroundColor = SoftCyan,
                                        textColor = NeobrutalBlack,
                                        isSelected = false
                                    )
                                }
                            }

                            items(state.orders) { order ->
                                OrderListItem(
                                    order = order,
                                    onViewQR = { onOrderClick(order.OrderId) },
                                    onReorder = { viewModel.reorderPastOrder(order, onCartReady) }
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

        // Reorder Toast Banner
        AnimatedVisibility(
            visible = reorderToastMessage != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NeobrutalCard(
                backgroundColor = SoftCyan,
                borderColor = NeobrutalBlack,
                borderWidth = 2.dp,
                shadowOffset = 4.dp,
                cornerRadius = 14.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeobrutalBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reorderToastMessage ?: "",
                        fontWeight = FontWeight.Black,
                        color = NeobrutalBlack,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun OrderListItem(order: Order, onViewQR: () -> Unit, onReorder: () -> Unit) {
    val isDark = LocalDarkTheme.current
    val textColor = if (isDark) DarkTextPrimary else NeobrutalBlack
    val savedItems = order.purchasedItems.orEmpty()
    NeobrutalCard(
        backgroundColor = if (isDark) DarkCardBg else NeobrutalWhite,
        modifier = Modifier.fillMaxWidth().height(292.dp).clickable(onClick = onViewQR),
        cornerRadius = 16.dp
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Order #${order.OrderId}", color = textColor, fontWeight = FontWeight.Black,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(order.OrderDate, color = textColor, style = MaterialTheme.typography.bodySmall)
                }
                NeobrutalStatusBadge(order.Status)
            }
            Text("₹${"%.2f".format(order.NetAmount)}", color = textColor,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (savedItems.isNotEmpty()) {
                    items(savedItems) { line ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${line.item.meitdes} ×${line.quantity}", modifier = Modifier.weight(1f),
                                color = textColor, style = MaterialTheme.typography.bodySmall)
                            Text("₹${"%.2f".format(line.lineTotal)}", color = textColor,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    item {
                        Text(order.sname.ifBlank { "Item details unavailable for this older order." },
                            color = textColor, style = MaterialTheme.typography.bodySmall)
                        Text("Exact quantities were not provided by Proodle.", color = textColor,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeobrutalButton(onClick = onReorder, enabled = savedItems.isNotEmpty(),
                    backgroundColor = PastelYellow, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Reorder", fontWeight = FontWeight.Bold)
                }
                NeobrutalButton(onClick = onViewQR, backgroundColor = MintGreen,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("View QR", fontWeight = FontWeight.Bold)
                }
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
