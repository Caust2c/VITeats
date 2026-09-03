package com.viteats.app.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
    val scrollState = rememberScrollState()
    val isDark = LocalDarkTheme.current

    val screenBg = if (isDark) DarkCharcoalBg else LavenderBackground
    val cardBg = if (isDark) DarkCardBg else NeobrutalWhite
    val textPrimary = if (isDark) DarkTextPrimary else NeobrutalBlack

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

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- Budget Management Component ---
                        BudgetManagementSection(
                            currentSpending = 2450.0,
                            initialBudget = 3000.0
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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

@Composable
fun BudgetManagementSection(
    currentSpending: Double = 2450.0,
    initialBudget: Double = 3000.0
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("viteats_budget_prefs", android.content.Context.MODE_PRIVATE) }

    var budgetInput by rememberSaveable {
        mutableStateOf(prefs.getString("monthly_budget", initialBudget.toInt().toString()) ?: "3000")
    }

    val budgetAmount = budgetInput.toDoubleOrNull() ?: initialBudget
    val spendingRatio = if (budgetAmount > 0) (currentSpending / budgetAmount).toFloat() else 0f
    val spendingPercentage = (spendingRatio * 100f).coerceAtLeast(0f)
    val isApproachingLimit = spendingPercentage >= 80f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(color = NeobrutalBlack.copy(alpha = 0.2f), thickness = 1.5.dp)

        // Title and Spending Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = NeobrutalBlack,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Monthly Spending",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = NeobrutalBlack
                )
            }

            Text(
                text = "₹${"%.0f".format(currentSpending)} / ₹${"%.0f".format(budgetAmount)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = if (isApproachingLimit) Color(0xFFDC2626) else NeobrutalBlack
            )
        }

        // Neobrutalist Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NeobrutalWhite)
                .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(spendingRatio.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(if (isApproachingLimit) Color(0xFFEF4444) else MintGreen)
            )
        }

        // Interactive Input Field: Set Monthly Budget Limit with Thick Black Border
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Monthly Limit (₹):",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = NeobrutalBlack
            )

            // Styled interactive input field with thick black border
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeobrutalWhite)
                    .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                BasicTextField(
                    value = budgetInput,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }.take(6)
                        budgetInput = filtered
                        prefs.edit().putString("monthly_budget", filtered).apply()
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = NeobrutalBlack
                    ),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹ ",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = NeobrutalBlack
                            )
                            if (budgetInput.isEmpty()) {
                                Text(
                                    text = "3000",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        // Conditional Warning Banner (>= 80% of budget)
        if (isApproachingLimit) {
            NeobrutalCard(
                backgroundColor = Color(0xFFEF4444),
                borderColor = NeobrutalBlack,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                cornerRadius = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = if (currentSpending > budgetAmount) "Budget Limit Exceeded!" else "Approaching Budget Limit!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "${"%.0f".format(spendingPercentage)}% of your ₹${"%.0f".format(budgetAmount)} limit used",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}



