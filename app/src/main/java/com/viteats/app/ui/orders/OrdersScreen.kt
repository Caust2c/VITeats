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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(viewModel: OrdersViewModel, onOrderClick: (String) -> Unit) {
    val ordersState by viewModel.ordersState.collectAsState()
    var reorderToastMessage by remember { mutableStateOf<String?>(null) }

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
            .background(LavenderBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (val state = ordersState) {
                is OrdersState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeobrutalBlack)
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
                                backgroundColor = NeobrutalWhite,
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
                                        color = NeobrutalBlack,
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
                                    onReorder = { viewModel.reorderPastOrder(order) }
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
                backgroundColor = MintGreen,
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
                        imageVector = Icons.Default.CheckCircle,
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
fun OrderListItem(
    order: Order,
    onViewQR: () -> Unit,
    onReorder: () -> Unit
) {
    val qrAvailable = isQrAvailable(order)
    var isExpanded by remember { mutableStateOf(false) }

    // Parse items list and formatted summary
    val parsedItems = remember(order.sname) {
        if (order.sname.isBlank()) listOf("Mess Meal")
        else order.sname.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val summaryText = remember(parsedItems) {
        parsedItems.joinToString(", ") { "$it x1" }
    }

    NeobrutalCard(
        backgroundColor = NeobrutalWhite,
        borderColor = NeobrutalBlack,
        borderWidth = 2.dp,
        shadowOffset = 4.dp,
        cornerRadius = 16.dp,
        modifier = Modifier.animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.OrderDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Order #${order.OrderId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        fontWeight = FontWeight.Medium
                    )
                }

                NeobrutalStatusBadge(status = order.Status)
            }

            // Order Content Summary & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (order.sname.isNotBlank()) order.sname else "Mess Order",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeobrutalBlack
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Truncated Order Summary string (replaces orderee name)
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "₹${order.NetAmount.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = NeobrutalBlack
                )
            }

            // "View Items" Accordion Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpanded) SoftCyan else LavenderCard)
                        .border(BorderStroke(1.5.dp, NeobrutalBlack), RoundedCornerShape(8.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Items" else "View Items",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = NeobrutalBlack
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Hide items" else "View items",
                        tint = NeobrutalBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expanded Accordion List of Purchased Items
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LavenderCard)
                        .border(BorderStroke(1.5.dp, NeobrutalBlack), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PURCHASED ITEMS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MutedText,
                        letterSpacing = 0.5.sp
                    )
                    HorizontalDivider(color = NeobrutalBlack.copy(alpha = 0.15f), thickness = 1.dp)

                    parsedItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(NeobrutalBlack, shape = RoundedCornerShape(50))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeobrutalBlack
                                )
                            }
                            Text(
                                text = "x1",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeobrutalBlack
                            )
                        }
                    }
                }
            }

            // Action Buttons: 1-Click Reorder & View QR (if available)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compact Auto-width Yellow "1-Click Reorder" button
                NeobrutalButton(
                    onClick = onReorder,
                    backgroundColor = PastelYellow,
                    contentColor = NeobrutalBlack,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp,
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = NeobrutalBlack
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "1-Click Reorder",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // QR Code Button (if active on the same day)
                if (qrAvailable) {
                    NeobrutalButton(
                        onClick = onViewQR,
                        backgroundColor = MintGreen,
                        contentColor = NeobrutalBlack,
                        borderColor = NeobrutalBlack,
                        borderWidth = 2.dp,
                        shadowOffset = 2.dp,
                        cornerRadius = 8.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "View QR",
                            modifier = Modifier.size(15.dp),
                            tint = NeobrutalBlack
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "QR",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
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


