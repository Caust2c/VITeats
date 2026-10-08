package com.viteats.app.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.viteats.app.VITeatsApplication
import com.viteats.app.data.model.CartItem
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckoutSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as VITeatsApplication
    val cartItems by viewModel.cartItems.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf(app.sessionManager.cachedPin ?: "") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val totalAmount = remember(cartItems) { cartItems.sumOf { it.lineTotal } }
    val totalCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            val orderId = (checkoutState as CheckoutState.Success).orderId
            viewModel.resetCheckoutState()
            onCheckoutSuccess(orderId)
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
                TopAppBar(
                    title = {
                        Text(
                            "My Cart",
                            fontWeight = FontWeight.Black,
                            color = NeobrutalBlack
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeobrutalBlack
                            )
                        }
                    },
                    actions = {
                        if (cartItems.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearCart() }) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = "Clear cart",
                                    tint = NeobrutalBlack
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NeobrutalWhite)
                )
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = NeobrutalBlack,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 3f
                            )
                        },
                    color = NeobrutalWhite
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Payable",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MutedText
                            )
                            Text(
                                text = "₹${"%.2f".format(totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NeobrutalBlack
                            )
                        }

                        NeobrutalButton(
                            onClick = {
                                enteredPin = app.sessionManager.cachedPin ?: ""
                                showPinDialog = true
                            },
                            backgroundColor = MintGreen,
                            contentColor = NeobrutalBlack,
                            shadowOffset = 3.dp,
                            cornerRadius = 14.dp,
                            enabled = checkoutState !is CheckoutState.Processing,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            if (checkoutState is CheckoutState.Processing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = NeobrutalBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = NeobrutalBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay & Order", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Error Banner
            if (checkoutState is CheckoutState.Error) {
                NeobrutalCard(
                    backgroundColor = SoftCoral,
                    shadowOffset = 4.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = NeobrutalBlack
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = (checkoutState as CheckoutState.Error).message,
                            color = NeobrutalBlack,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (cartItems.isEmpty()) {
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
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = NeobrutalBlack
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your Cart is Empty",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NeobrutalBlack
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Explore the mess menu to add items.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedText
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            NeobrutalButton(
                                onClick = onBack,
                                backgroundColor = PastelYellow
                            ) {
                                Text("Browse Menu", fontWeight = FontWeight.Black)
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
                    item {
                        Text(
                            text = "Items in Cart ($totalCount)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeobrutalBlack
                        )
                    }

                    items(cartItems, key = { it.item.meitid }) { cartItem ->
                        NeobrutalCard(
                            backgroundColor = NeobrutalWhite,
                            shadowOffset = 3.dp,
                            cornerRadius = 14.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(LavenderBackground)
                                        .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(10.dp))
                                ) {
                                    AsyncImage(
                                        model = cartItem.item.imageUrl,
                                        contentDescription = cartItem.item.meitdes,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cartItem.item.meitdes,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        color = NeobrutalBlack
                                    )
                                    Text(
                                        text = "₹${"%.2f".format(cartItem.item.retrt)} each",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                    Text(
                                        text = "₹${"%.2f".format(cartItem.lineTotal)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = NeobrutalBlack
                                    )
                                }

                                // Stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MintGreen)
                                        .border(BorderStroke(1.5.dp, NeobrutalBlack), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.decrementItem(cartItem.item) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (cartItem.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            modifier = Modifier.size(14.dp),
                                            tint = NeobrutalBlack
                                        )
                                    }

                                    Text(
                                        text = "${cartItem.quantity}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = NeobrutalBlack,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.addItem(cartItem.item) },
                                        modifier = Modifier.size(26.dp),
                                        enabled = cartItem.quantity < cartItem.item.StockQty
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Increase",
                                            modifier = Modifier.size(14.dp),
                                            tint = NeobrutalBlack
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Bill Breakdown Neobrutal Card
                        NeobrutalCard(
                            backgroundColor = PastelYellow,
                            borderColor = NeobrutalBlack,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp,
                            cornerRadius = 16.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Bill Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeobrutalBlack
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Items Subtotal", color = NeobrutalBlack, fontWeight = FontWeight.Medium)
                                    Text("₹${"%.2f".format(totalAmount)}", fontWeight = FontWeight.Bold, color = NeobrutalBlack)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mess / Platform Fee", color = NeobrutalBlack, fontWeight = FontWeight.Medium)
                                    Text("FREE", color = Color(0xFF15803D), fontWeight = FontWeight.Black)
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = NeobrutalBlack.copy(alpha = 0.2f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Amount", fontWeight = FontWeight.Black, color = NeobrutalBlack)
                                    Text(
                                        "₹${"%.2f".format(totalAmount)}",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = NeobrutalBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // PIN Confirmation Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                if (checkoutState !is CheckoutState.Processing) {
                    showPinDialog = false
                }
            },
            title = {
                Text(
                    text = "Confirm Mess PIN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NeobrutalBlack
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your Proodle PIN to authorize payment of ₹${"%.2f".format(totalAmount)} from your wallet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = null
                        },
                        label = { Text("6-Digit PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError != null,
                        singleLine = true
                    )

                    if (pinError != null) {
                        Text(
                            text = pinError!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                NeobrutalButton(
                    onClick = {
                        if (enteredPin.isBlank()) {
                            pinError = "Please enter your PIN"
                        } else {
                            showPinDialog = false
                            viewModel.checkout(enteredPin)
                        }
                    },
                    backgroundColor = MintGreen,
                    enabled = checkoutState !is CheckoutState.Processing
                ) {
                    Text("Authorize & Pay", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPinDialog = false },
                    enabled = checkoutState !is CheckoutState.Processing
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = NeobrutalBlack)
                }
            }
        )
    }
}

