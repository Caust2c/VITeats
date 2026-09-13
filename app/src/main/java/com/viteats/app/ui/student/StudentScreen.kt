package com.viteats.app.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.components.NeobrutalPill
import com.viteats.app.ui.theme.*
import com.viteats.app.util.MealPeriodHelper
import com.viteats.app.util.MealType
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar

enum class FinancialHealthStatus(
    val label: String,
    val color: Color
) {
    ON_TRACK("On Track", Color(0xFFA8F0D4)),
    PACING_FAST("Pacing Fast", Color(0xFFFFD166)),
    OVER_BUDGET("Over Budget", Color(0xFFFF6B6B))
}

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
    val textMuted = if (isDark) DarkTextSecondary else MutedText

    var mealStatus by remember { mutableStateOf(MealPeriodHelper.getCurrentMealStatus()) }

    LaunchedEffect(Unit) {
        mealStatus = MealPeriodHelper.getCurrentMealStatus()
        viewModel.fetchOrdersAndExpenses()
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

                // Collect dynamic allocation & spending from ViewModel
                val initialMonthlyAllocation by viewModel.initialMonthlyAllocation.collectAsState()
                val currentSpending by viewModel.currentSpending.collectAsState()
                val isMonthRolledOver by viewModel.isMonthRolledOver.collectAsState()

                var isEditingBudget by remember { mutableStateOf(isMonthRolledOver || initialMonthlyAllocation <= 0.0) }

                // --- Dynamic "Financial Health" Dashboard Card ---
                val remainingBudget = balance.bal
                val budgetProgress = if (initialMonthlyAllocation > 0) {
                    (currentSpending / initialMonthlyAllocation).toFloat().coerceIn(0f, 1f)
                } else 0f

                val today = remember { LocalDate.now() }
                val yearMonth = remember(today) { YearMonth.from(today) }
                val daysInMonth = remember(yearMonth) { yearMonth.lengthOfMonth() }
                val currentDay = remember(today) { today.dayOfMonth }
                val daysLeftInMonth = remember(daysInMonth, currentDay) { (daysInMonth - currentDay + 1).coerceAtLeast(1) }
                val safeDailySpend = remember(remainingBudget, daysLeftInMonth) {
                    if (daysLeftInMonth > 0) (remainingBudget / daysLeftInMonth).coerceAtLeast(0.0) else 0.0
                }
                val daysPassed = remember(currentDay) { currentDay.coerceAtLeast(1) }
                val currentDailyAverage = remember(currentSpending, daysPassed) {
                    if (daysPassed > 0) (currentSpending / daysPassed).coerceAtLeast(0.0) else 0.0
                }

                val healthStatus = remember(safeDailySpend, currentDailyAverage, remainingBudget, initialMonthlyAllocation, currentSpending) {
                    if (remainingBudget <= 0.0 || (initialMonthlyAllocation > 0 && currentSpending >= initialMonthlyAllocation) || currentDailyAverage > safeDailySpend * 1.20) {
                        FinancialHealthStatus.OVER_BUDGET
                    } else if (currentDailyAverage > safeDailySpend) {
                        FinancialHealthStatus.PACING_FAST
                    } else {
                        FinancialHealthStatus.ON_TRACK
                    }
                }

                val formattedSafeDailySpend = remember(safeDailySpend) {
                    java.text.NumberFormat.getIntegerInstance().format(safeDailySpend.toInt())
                }
                val formattedRemainingBudget = remember(remainingBudget) {
                    java.text.NumberFormat.getIntegerInstance().format(remainingBudget.toInt().coerceAtLeast(0))
                }

                NeobrutalCard(
                    backgroundColor = cardBg,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.5.dp,
                    shadowOffset = 5.dp,
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header with Title, Dynamic Status Badge, and "Edit Budget" Pencil Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Financial Health",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = textPrimary
                                )

                                // Dynamic Status-Driven Accent Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(healthStatus.color)
                                        .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = healthStatus.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = NeobrutalBlack
                                    )
                                }
                            }

                            // "Edit Budget" Pencil Icon Button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .offset(x = (-2).dp, y = (-2).dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .offset(x = 3.dp, y = 3.dp)
                                        .background(NeobrutalBlack, RoundedCornerShape(8.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isEditingBudget) PastelYellow else (if (isDark) DarkCardBg else NeobrutalWhite))
                                        .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(8.dp))
                                        .clickable { isEditingBudget = !isEditingBudget },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Budget",
                                        tint = if (isDark && !isEditingBudget) DarkTextPrimary else NeobrutalBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Inline Budget Setting & Editing Block
                        if (isEditingBudget) {
                            var budgetInputText by remember(initialMonthlyAllocation) {
                                mutableStateOf(if (initialMonthlyAllocation > 0) initialMonthlyAllocation.toInt().toString() else "")
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color(0xFF24303F) else LavenderCard)
                                    .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = if (isMonthRolledOver) "New Month! Set Monthly Budget" else "Set Monthly Budget",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = budgetInputText,
                                            onValueChange = { budgetInputText = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Budget", fontWeight = FontWeight.Bold) },
                                            prefix = { Text("₹", fontWeight = FontWeight.Bold, color = textPrimary) },
                                            placeholder = { Text("8000") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = textPrimary,
                                                unfocusedTextColor = textPrimary,
                                                focusedBorderColor = NeobrutalBlack,
                                                unfocusedBorderColor = NeobrutalBlack.copy(alpha = 0.6f),
                                                focusedContainerColor = if (isDark) DarkCardBg else NeobrutalWhite,
                                                unfocusedContainerColor = if (isDark) DarkCardBg else NeobrutalWhite
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        NeobrutalButton(
                                            onClick = {
                                                val enteredBudget = budgetInputText.toDoubleOrNull()
                                                if (enteredBudget != null && enteredBudget > 0) {
                                                    viewModel.setInitialMonthlyAllocation(enteredBudget)
                                                }
                                                isEditingBudget = false
                                            },
                                            backgroundColor = MintGreen,
                                            contentColor = NeobrutalBlack,
                                            borderColor = NeobrutalBlack,
                                            borderWidth = 2.dp,
                                            shadowOffset = 2.dp,
                                            cornerRadius = 8.dp,
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                                        ) {
                                            Text(
                                                text = "Save",
                                                fontWeight = FontWeight.Black,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }

                                        if (initialMonthlyAllocation > 0) {
                                            TextButton(
                                                onClick = { isEditingBudget = false },
                                                contentPadding = PaddingValues(horizontal = 6.dp)
                                            ) {
                                                Text("Cancel", fontWeight = FontWeight.Bold, color = textMuted)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Prominent "Safe Daily Pacing" Metric Hero Block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when (healthStatus) {
                                        FinancialHealthStatus.ON_TRACK -> if (isDark) Color(0xFF16382A) else Color(0xFFEDFDF5)
                                        FinancialHealthStatus.PACING_FAST -> if (isDark) Color(0xFF382C14) else Color(0xFFFFFBEB)
                                        FinancialHealthStatus.OVER_BUDGET -> if (isDark) Color(0xFF381717) else Color(0xFFFEF2F2)
                                    }
                                )
                                .border(BorderStroke(2.5.dp, NeobrutalBlack), RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "₹$formattedSafeDailySpend",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "/ day safe to spend",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = when (healthStatus) {
                                            FinancialHealthStatus.ON_TRACK -> if (isDark) Color(0xFF86EFAC) else Color(0xFF16A34A)
                                            FinancialHealthStatus.PACING_FAST -> if (isDark) Color(0xFFFCD34D) else Color(0xFFD97706)
                                            FinancialHealthStatus.OVER_BUDGET -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                                        }
                                    )
                                }

                                Text(
                                    text = "₹$formattedRemainingBudget total remaining",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textMuted
                                )
                            }
                        }

                        // Horizontal Progress Bar with Thick Black Border
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF374151) else Color(0xFFE2E8F0))
                                .border(BorderStroke(2.5.dp, NeobrutalBlack), RoundedCornerShape(10.dp))
                        ) {
                            if (budgetProgress > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(budgetProgress)
                                        .fillMaxHeight()
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 8.dp,
                                                bottomStart = 8.dp,
                                                topEnd = if (budgetProgress >= 0.95f) 8.dp else 0.dp,
                                                bottomEnd = if (budgetProgress >= 0.95f) 8.dp else 0.dp
                                            )
                                        )
                                        .background(
                                            when (healthStatus) {
                                                FinancialHealthStatus.ON_TRACK -> Color(0xFF22C55E)
                                                FinancialHealthStatus.PACING_FAST -> Color(0xFFF59E0B)
                                                FinancialHealthStatus.OVER_BUDGET -> Color(0xFFEF4444)
                                            }
                                        )
                                        .border(BorderStroke(1.dp, NeobrutalBlack))
                                )
                            }
                        }

                        // Subtitle row displaying spent and usage percentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (initialMonthlyAllocation > 0) {
                                    "₹${currentSpending.toInt()} Spent of ₹${initialMonthlyAllocation.toInt()} Budget"
                                } else {
                                    "₹${currentSpending.toInt()} Spent (Budget not set)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = textMuted
                            )

                            Text(
                                text = "${(budgetProgress * 100).toInt()}% Used",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )
                        }
                    }
                }

                // --- Expense Breakdown Card (Dynamic Order History Aggregation) ---
                val categorySpending by viewModel.categorySpending.collectAsState()

                val expenseCategories = remember(categorySpending) {
                    listOf(
                        ExpenseCategory("Lunch", categorySpending.lunch, MintGreen, categorySpending.lunchPercentage),
                        ExpenseCategory("Dinner", categorySpending.dinner, SoftCyan, categorySpending.dinnerPercentage),
                        ExpenseCategory("Snacks", categorySpending.snacks, PastelYellow, categorySpending.snacksPercentage),
                        ExpenseCategory("Breakfast", categorySpending.breakfast, SoftCoral, categorySpending.breakfastPercentage)
                    )
                }

                NeobrutalCard(
                    backgroundColor = cardBg,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.5.dp,
                    shadowOffset = 5.dp,
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expense Breakdown",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Donut Chart
                            NeobrutalExpenseDonutChart(
                                categories = expenseCategories,
                                modifier = Modifier.size(130.dp),
                                strokeWidth = 22.dp
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Spent",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textMuted
                                    )
                                    Text(
                                        text = "₹${categorySpending.totalSpent.toInt()}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            // Bold Legend
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                expenseCategories.forEach { cat ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(cat.color)
                                                    .border(1.5.dp, NeobrutalBlack, RoundedCornerShape(3.dp))
                                            )
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }

                                        Text(
                                            text = "₹${cat.amount.toInt()}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
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
                            value = balance.custid.ifBlank { balance.regno.ifBlank { "2000123234" } }
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

data class ExpenseCategory(
    val name: String,
    val amount: Double,
    val color: Color,
    val percentage: Int = 0
)

@Composable
fun NeobrutalExpenseDonutChart(
    categories: List<ExpenseCategory>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 22.dp,
    centerContent: (@Composable () -> Unit)? = null
) {
    val total = remember(categories) { categories.sumOf { it.amount } }
    val textMeasurer = rememberTextMeasurer()
    val isDark = LocalDarkTheme.current
    val adjacentLabelColor = if (isDark) DarkTextPrimary else NeobrutalBlack

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val strokeWidthPx = strokeWidth.toPx()
            val borderStrokePx = 2.dp.toPx()
            val outerRadius = size.minDimension / 2f
            val innerRadius = outerRadius - strokeWidthPx
            val arcSize = size.minDimension - strokeWidthPx
            val topLeftOffset = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            var startAngle = -90f

            data class SegmentLabel(
                val pct: Int,
                val midAngle: Float,
                val isInside: Boolean
            )
            val labelsToDraw = mutableListOf<SegmentLabel>()

            // Draw colored segment arcs
            categories.forEach { item ->
                val sweepAngle = if (total > 0) ((item.amount / total) * 360f).toFloat() else 0f
                if (sweepAngle > 0f) {
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                    )

                    val pct = if (item.percentage > 0) item.percentage else if (total > 0) ((item.amount / total) * 100).toInt() else 0
                    if (pct > 0) {
                        val midAngle = startAngle + sweepAngle / 2f
                        val isInside = sweepAngle >= 20f
                        labelsToDraw.add(SegmentLabel(pct, midAngle, isInside))
                    }

                    startAngle += sweepAngle
                }
            }

            // Neobrutalist outer circle border
            drawCircle(
                color = NeobrutalBlack,
                radius = outerRadius,
                style = Stroke(width = borderStrokePx)
            )

            // Neobrutalist inner circle border
            if (innerRadius > 0f) {
                drawCircle(
                    color = NeobrutalBlack,
                    radius = innerRadius,
                    style = Stroke(width = borderStrokePx)
                )
            }

            // Draw percentage values directly inside or adjacent to segments
            labelsToDraw.forEach { label ->
                val angleRad = Math.toRadians(label.midAngle.toDouble())
                val cosA = kotlin.math.cos(angleRad).toFloat()
                val sinA = kotlin.math.sin(angleRad).toFloat()

                val textToDraw = "${label.pct}%"
                val textLayout = textMeasurer.measure(
                    text = textToDraw,
                    style = TextStyle(
                        fontSize = if (label.isInside) 8.5.sp else 7.5.sp,
                        fontWeight = FontWeight.Black,
                        color = if (label.isInside) NeobrutalBlack else adjacentLabelColor
                    )
                )

                if (label.isInside) {
                    val ringMidRadius = (outerRadius + innerRadius) / 2f
                    val posX = centerOffset.x + (ringMidRadius * cosA)
                    val posY = centerOffset.y + (ringMidRadius * sinA)

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            posX - textLayout.size.width / 2f,
                            posY - textLayout.size.height / 2f
                        )
                    )
                } else {
                    val tickStart = outerRadius
                    val tickEnd = outerRadius + 2.5.dp.toPx()
                    drawLine(
                        color = NeobrutalBlack,
                        start = Offset(centerOffset.x + tickStart * cosA, centerOffset.y + tickStart * sinA),
                        end = Offset(centerOffset.x + tickEnd * cosA, centerOffset.y + tickEnd * sinA),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val labelRadius = outerRadius + 7.dp.toPx()
                    val posX = centerOffset.x + (labelRadius * cosA)
                    val posY = centerOffset.y + (labelRadius * sinA)

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            posX - textLayout.size.width / 2f,
                            posY - textLayout.size.height / 2f
                        )
                    )
                }
            }
        }

        centerContent?.invoke()
    }
}



