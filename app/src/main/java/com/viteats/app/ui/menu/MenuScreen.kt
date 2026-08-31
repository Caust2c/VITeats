package com.viteats.app.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.viteats.app.data.remote.MenuItem
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.components.NeobrutalPill
import com.viteats.app.ui.theme.*

@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    onNavigateToCart: () -> Unit = {}
) {
    val menuState by viewModel.menuState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val totalCartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }
    val totalCartAmount = remember(cartItems) { cartItems.sumOf { it.lineTotal } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LavenderBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Neobrutal Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                NeobrutalCard(
                    backgroundColor = NeobrutalWhite,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeobrutalBlack,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search dishes, combos, outlets...",
                                    color = MutedText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = NeobrutalBlack,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChanged("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = NeobrutalBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            when (val state = menuState) {
                is MenuState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeobrutalBlack)
                    }
                }
                is MenuState.Success -> {
                    val categories = remember(state.categories) {
                        listOf("All") + state.categories.map { it.skname }.distinct()
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            NeobrutalPill(
                                text = category,
                                backgroundColor = if (isSelected) MintGreen else NeobrutalWhite,
                                textColor = NeobrutalBlack,
                                isSelected = isSelected,
                                onClick = { viewModel.onCategorySelected(category) }
                            )
                        }
                    }

                    val filteredItems = remember(state.items, selectedCategory, searchQuery) {
                        state.items.filter { item ->
                            val matchesCategory = selectedCategory == "All" || item.skudes.equals(selectedCategory, ignoreCase = true)
                            val matchesQuery = searchQuery.isBlank() ||
                                    item.meitdes.contains(searchQuery, ignoreCase = true) ||
                                    item.dispname.contains(searchQuery, ignoreCase = true) ||
                                    item.skudes.contains(searchQuery, ignoreCase = true)
                            matchesCategory && matchesQuery
                        }
                    }

                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            NeobrutalCard(
                                backgroundColor = NeobrutalWhite,
                                shadowOffset = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = NeobrutalBlack
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "No items found matching \"$searchQuery\""
                                        else "No items in this category",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NeobrutalBlack
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = if (totalCartCount > 0) 96.dp else 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredItems, key = { it.meitid }) { item ->
                                val inCartQty = cartItems.find { it.item.meitid == item.meitid }?.quantity ?: 0
                                MenuItemCard(
                                    item = item,
                                    quantityInCart = inCartQty,
                                    onAddToCart = { viewModel.addToCart(item) },
                                    onDecrement = { viewModel.decrementItem(item) }
                                )
                            }
                        }
                    }
                }
                is MenuState.Error -> {
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
                                    onClick = { viewModel.fetchMenu() },
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

        // --- Floating Neobrutal Cart Bar ---
        AnimatedVisibility(
            visible = totalCartCount > 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NeobrutalCard(
                backgroundColor = PastelYellow,
                borderColor = NeobrutalBlack,
                borderWidth = 2.5.dp,
                shadowOffset = 5.dp,
                cornerRadius = 18.dp,
                onClick = onNavigateToCart
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
                            text = "$totalCartCount item${if (totalCartCount > 1) "s" else ""} added",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeobrutalBlack
                        )
                        Text(
                            text = "₹${"%.2f".format(totalCartAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = NeobrutalBlack
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintGreen)
                            .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "View Cart",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = NeobrutalBlack
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Cart",
                            tint = NeobrutalBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantityInCart: Int = 0,
    onAddToCart: () -> Unit = {},
    onDecrement: () -> Unit = {}
) {
    val isOutOfStock = item.StockQty <= 0

    NeobrutalCard(
        backgroundColor = if (isOutOfStock) NeobrutalWhite.copy(alpha = 0.8f) else NeobrutalWhite,
        borderColor = NeobrutalBlack,
        borderWidth = 2.dp,
        shadowOffset = if (isOutOfStock) 2.dp else 4.dp,
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .alpha(if (isOutOfStock) 0.65f else 1.0f)
        ) {
            // Food Image with status overlay
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LavenderBackground)
                    .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.meitdes,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SOLD OUT",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.meitdes,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = NeobrutalBlack
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${item.dispname} · ${item.skudes}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MutedText
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SoftCyan)
                                .border(BorderStroke(1.dp, NeobrutalBlack), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.odtdes,
                                style = MaterialTheme.typography.labelSmall,
                                color = NeobrutalBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!isOutOfStock && item.StockQty in 1..5) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Only ${item.StockQty} left",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC2410C),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${"%.2f".format(item.retrt)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeobrutalBlack
                    )

                    if (isOutOfStock) {
                        Text(
                            text = "Unavailable",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                    } else if (quantityInCart == 0) {
                        NeobrutalButton(
                            onClick = onAddToCart,
                            backgroundColor = PastelYellow,
                            borderWidth = 1.5.dp,
                            shadowOffset = 2.dp,
                            cornerRadius = 8.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeobrutalBlack)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("ADD", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        // Neobrutal Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MintGreen)
                                .border(BorderStroke(1.5.dp, NeobrutalBlack), RoundedCornerShape(8.dp))
                                .padding(horizontal = 2.dp, vertical = 1.dp)
                        ) {
                            IconButton(
                                onClick = onDecrement,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (quantityInCart == 1) Icons.Default.Delete else Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    modifier = Modifier.size(14.dp),
                                    tint = NeobrutalBlack
                                )
                            }

                            Text(
                                text = "$quantityInCart",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = NeobrutalBlack,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            IconButton(
                                onClick = onAddToCart,
                                modifier = Modifier.size(24.dp),
                                enabled = quantityInCart < item.StockQty
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
        }
    }
}


