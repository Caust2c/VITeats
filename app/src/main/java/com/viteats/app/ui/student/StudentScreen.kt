package com.viteats.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.components.NeobrutalPill
import com.viteats.app.ui.theme.*
import com.viteats.app.util.MealPeriodHelper
import com.viteats.app.util.MealType

@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    onNavigateToTab: ((Int) -> Unit)? = null
) {
    val balanceState by viewModel.balanceState.collectAsState()
    val scrollState = rememberScrollState()

    var mealStatus by remember { mutableStateOf(MealPeriodHelper.getCurrentMealStatus()) }

    LaunchedEffect(Unit) {
        mealStatus = MealPeriodHelper.getCurrentMealStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LavenderBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Header with Meal Schedule Pills ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "VITeats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = NeobrutalBlack,
                letterSpacing = (-0.5).sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(MealType.allMeals()) { meal ->
                    val isActive = meal == mealStatus.activeMeal
                    val pillBg = when {
                        isActive -> MintGreen
                        meal == MealType.BREAKFAST -> SoftCyan
                        meal == MealType.LUNCH -> PastelYellow
                        meal == MealType.SNACKS -> SoftCoral
                        else -> SoftOrange
                    }

                    NeobrutalPill(
                        text = meal.displayName,
                        backgroundColor = pillBg,
                        textColor = NeobrutalBlack,
                        isSelected = isActive,
                        onClick = { onNavigateToTab?.invoke(1) }
                    )
                }
            }
        }

        // --- Large Pale Yellow Wallet Balance Hero Card ---
        when (val state = balanceState) {
            is BalanceState.Loading -> {
                NeobrutalCard(
                    backgroundColor = PastelYellow,
                    shadowOffset = 5.dp,
                    cornerRadius = 20.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeobrutalBlack)
                    }
                }
            }
            is BalanceState.Success -> {
                val balance = state.balance

                NeobrutalCard(
                    backgroundColor = PastelYellow,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.5.dp,
                    shadowOffset = 5.dp,
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Text(
                            text = "Wallet Balance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeobrutalBlack
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "₹${"%.2f".format(balance.bal)}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = NeobrutalBlack,
                            letterSpacing = (-1).sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "Active" Badge
                            NeobrutalPill(
                                text = "Active",
                                backgroundColor = MintGreen,
                                textColor = NeobrutalBlack,
                                isSelected = false
                            )

                            // Bold "Order" Button
                            NeobrutalButton(
                                onClick = { onNavigateToTab?.invoke(1) },
                                backgroundColor = MintGreen,
                                contentColor = NeobrutalBlack,
                                borderColor = NeobrutalBlack,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                cornerRadius = 12.dp,
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Order",
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                // --- White Card for "Account Details" ---
                NeobrutalCard(
                    backgroundColor = NeobrutalWhite,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.5.dp,
                    shadowOffset = 5.dp,
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Account Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = NeobrutalBlack
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        NeobrutalDetailRow(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = balance.email.ifBlank { "rahul.s@vit.ac.in" }
                        )

                        NeobrutalDetailRow(
                            icon = Icons.Outlined.CreditCard,
                            label = "Card No",
                            value = if (balance.cardno.isNotBlank()) "**** ${balance.cardno.takeLast(4)}" else "**** 1234"
                        )

                        NeobrutalDetailRow(
                            icon = Icons.Outlined.Person,
                            label = "Customer ID",
                            value = balance.custid.ifBlank { balance.regno.ifBlank { "21BCE1234" } }
                        )
                    }
                }
            }
            is BalanceState.Error -> {
                NeobrutalCard(
                    backgroundColor = SoftCoral,
                    shadowOffset = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = NeobrutalBlack,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NeobrutalButton(
                            onClick = { viewModel.fetchBalance() },
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

@Composable
fun NeobrutalDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = NeobrutalBlack
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = NeobrutalBlack
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = NeobrutalBlack
        )
    }
}


