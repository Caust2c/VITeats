package com.viteats.app.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val budgetLimit by viewModel.budgetLimit.collectAsState()
    val currentSpending by viewModel.currentSpending.collectAsState()
    var budgetInputText by remember(budgetLimit) { mutableStateOf(budgetLimit.toInt().toString()) }

    val scrollState = rememberScrollState()
    val isDark = LocalDarkTheme.current

    val screenBg = if (isDark) DarkCharcoalBg else LavenderBackground
    val cardBg = if (isDark) DarkCardBg else NeobrutalWhite
    val textPrimary = if (isDark) DarkTextPrimary else NeobrutalBlack
    val textMuted = if (isDark) DarkTextSecondary else MutedText

    var mealStatus by remember { mutableStateOf(MealPeriodHelper.getCurrentMealStatus()) }

    LaunchedEffect(Unit) {
        mealStatus = MealPeriodHelper.getCurrentMealStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Meal Schedule Pills ---
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

                // --- Neobrutalist "Monthly Budget" Card ---
                val progress = if (budgetLimit > 0) (currentSpending / budgetLimit).toFloat() else 0f
                val isNearingLimit = progress >= 0.8f

                NeobrutalCard(
                    backgroundColor = if (isDark) DarkCardBg else SoftCyan,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.5.dp,
                    shadowOffset = 5.dp,
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title & Status Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Budget",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )

                            NeobrutalPill(
                                text = "₹${"%.0f".format(currentSpending)} / ₹${"%.0f".format(budgetLimit)}",
                                backgroundColor = PastelYellow,
                                textColor = NeobrutalBlack,
                                isSelected = false
                            )
                        }

                        // Dynamic Warning Banner (if spending reaches or exceeds 80% of budget)
                        if (isNearingLimit) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SoftCoral)
                                    .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = NeobrutalBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Nearing Budget Limit!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = NeobrutalBlack
                                    )
                                }
                            }
                        }

                        // Custom Linear Progress Tracker
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF374151) else NeobrutalWhite)
                                    .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(8.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .background(MintGreen)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}% Spent (₹${"%.0f".format(currentSpending)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "₹${"%.0f".format((budgetLimit - currentSpending).coerceAtLeast(0.0))} Remaining",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textMuted
                                )
                            }
                        }

                        // Budget Setter: OutlinedTextField with thick border + Save Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = budgetInputText,
                                onValueChange = { budgetInputText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix = { Text("₹ ", fontWeight = FontWeight.Black, color = textPrimary) },
                                placeholder = { Text("Budget Limit", color = Color(0xFF64748B)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = if (isDark) Color(0xFF374151) else NeobrutalWhite,
                                    unfocusedContainerColor = if (isDark) Color(0xFF374151) else NeobrutalWhite,
                                    cursorColor = textPrimary
                                )
                            )

                            NeobrutalButton(
                                onClick = {
                                    val parsed = budgetInputText.toDoubleOrNull()
                                    if (parsed != null && parsed > 0) {
                                        viewModel.updateBudgetLimit(parsed)
                                    }
                                },
                                backgroundColor = PastelYellow,
                                contentColor = NeobrutalBlack,
                                borderColor = NeobrutalBlack,
                                borderWidth = 2.dp,
                                shadowOffset = 2.5.dp,
                                cornerRadius = 10.dp,
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "Save",
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                // --- White Card for "Account Details" ---
                NeobrutalCard(
                    backgroundColor = cardBg,
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
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        NeobrutalDetailRow(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = balance.email.ifBlank { "email.student@vit.ac.in" }
                        )

                        NeobrutalDetailRow(
                            icon = Icons.Outlined.CreditCard,
                            label = "Card No",
                            value = if (balance.cardno.isNotBlank()) balance.cardno.takeLast(4) else "1234"
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
    val isDark = LocalDarkTheme.current
    val textPrimary = if (isDark) DarkTextPrimary else NeobrutalBlack

    val breakableValue = remember(value) {
        value.replace("@", "@\u200B")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = textPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                fontSize = 13.5.sp
            )
        }

        Text(
            text = breakableValue,
            style = MaterialTheme.typography.bodySmall.copy(
                lineBreak = androidx.compose.ui.text.style.LineBreak.Simple
            ),
            fontWeight = FontWeight.SemiBold,
            color = textPrimary,
            fontSize = 13.5.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            softWrap = true
        )
    }
}


