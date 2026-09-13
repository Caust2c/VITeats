package com.viteats.app.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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

                var allocationInputText by remember { mutableStateOf("") }
                var isEditingAllocation by remember { mutableStateOf(false) }

                // --- Monthly Rollover or Unset Allocation Prompt Card ---
                if (isMonthRolledOver || initialMonthlyAllocation <= 0.0 || isEditingAllocation) {
                    NeobrutalCard(
                        backgroundColor = if (isMonthRolledOver) PastelYellow else cardBg,
                        borderColor = NeobrutalBlack,
                        borderWidth = 2.5.dp,
                        shadowOffset = 5.dp,
                        cornerRadius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
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
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isMonthRolledOver) NeobrutalWhite else PastelYellow)
                                            .border(1.5.dp, NeobrutalBlack, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = NeobrutalBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = if (isMonthRolledOver) "New Month Started!"
                                        else if (initialMonthlyAllocation <= 0.0) "Set Monthly Allocation"
                                        else "Update Monthly Allocation",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isMonthRolledOver) NeobrutalBlack else textPrimary
                                    )
                                }

                                if (initialMonthlyAllocation > 0.0 && isEditingAllocation && !isMonthRolledOver) {
                                    TextButton(
                                        onClick = { isEditingAllocation = false },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Cancel", fontWeight = FontWeight.Bold, color = textMuted)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = allocationInputText,
                                    onValueChange = { input ->
                                        allocationInputText = input.filter { it.isDigit() || it == '.' }
                                    },
                                    placeholder = {
                                        Text(
                                            text = if (initialMonthlyAllocation > 0) "${initialMonthlyAllocation.toInt()}" else "9000",
                                            color = Color(0xFF64748B)
                                        )
                                    },
                                    prefix = { Text("₹", fontWeight = FontWeight.Bold, color = textPrimary) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary,
                                        focusedBorderColor = NeobrutalBlack,
                                        unfocusedBorderColor = NeobrutalBlack.copy(alpha = 0.6f),
                                        focusedContainerColor = if (isDark) Color(0xFF374151) else LavenderCard,
                                        unfocusedContainerColor = if (isDark) Color(0xFF374151) else LavenderCard
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                NeobrutalButton(
                                    onClick = {
                                        val entered = allocationInputText.toDoubleOrNull()
                                        if (entered != null && entered > 0) {
                                            viewModel.setInitialMonthlyAllocation(entered)
                                            allocationInputText = ""
                                            isEditingAllocation = false
                                        }
                                    },
                                    backgroundColor = MintGreen,
                                    contentColor = NeobrutalBlack,
                                    borderColor = NeobrutalBlack,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.dp,
                                    cornerRadius = 10.dp,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                                ) {
                                    Text(
                                        text = "Set Allocation",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Budget Tracking Card ---
                val remainingBudget = balance.bal
                val budgetProgress = if (initialMonthlyAllocation > 0) {
                    (currentSpending / initialMonthlyAllocation).toFloat().coerceIn(0f, 1f)
                } else 0f

                val today = remember { LocalDate.now() }
                val yearMonth = remember(today) { YearMonth.from(today) }
                val daysInMonth = remember(yearMonth) { yearMonth.lengthOfMonth() }
                val currentDay = remember(today) { today.dayOfMonth }
                val remainingDays = remember(daysInMonth, currentDay) { (daysInMonth - currentDay + 1).coerceAtLeast(1) }
                val perDayAmount = remember(remainingBudget, remainingDays) {
                    if (remainingDays > 0) remainingBudget / remainingDays else 0.0
                }
                val formattedDailySpend = remember(perDayAmount) {
                    String.format(java.util.Locale.US, "%.2f", perDayAmount)
                }

                NeobrutalCard(
                    backgroundColor = cardBg,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.5.dp,
                    shadowOffset = 5.dp,
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Budget Tracking",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )

                            NeobrutalPill(
                                text = if (initialMonthlyAllocation > 0) "₹${initialMonthlyAllocation.toInt()} Limit" else "Set Limit",
                                backgroundColor = PastelYellow,
                                textColor = NeobrutalBlack,
                                isSelected = false,
                                onClick = { isEditingAllocation = !isEditingAllocation }
                            )
                        }

                        // Horizontal Progress Bar with Thick Black Border & Vibrant Green Fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (isDark) Color(0xFF374151) else Color(0xFFE2E8F0))
                                .border(BorderStroke(2.5.dp, NeobrutalBlack), RoundedCornerShape(11.dp))
                        ) {
                            if (budgetProgress > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(budgetProgress)
                                        .fillMaxHeight()
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 9.dp,
                                                bottomStart = 9.dp,
                                                topEnd = if (budgetProgress >= 0.95f) 9.dp else 0.dp,
                                                bottomEnd = if (budgetProgress >= 0.95f) 9.dp else 0.dp
                                            )
                                        )
                                        .background(Color(0xFF22C55E)) // Vibrant green
                                        .border(BorderStroke(1.dp, NeobrutalBlack))
                                )
                            }
                        }

                        // Text Row Dynamically Displaying Remaining Budget
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${remainingBudget.toInt()} Remaining",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )

                            Text(
                                text = if (initialMonthlyAllocation > 0) {
                                    "₹${currentSpending.toInt()} Spent / ₹${initialMonthlyAllocation.toInt()}"
                                } else {
                                    "₹${currentSpending.toInt()} Spent (Set Allocation)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = textMuted
                            )
                        }

                        // Highlighted Actionable Insight Block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelYellow)
                                .border(BorderStroke(2.dp, NeobrutalBlack), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeobrutalWhite)
                                        .border(1.5.dp, NeobrutalBlack, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = NeobrutalBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "You can spend ₹$formattedDailySpend per day for the rest of the month",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = NeobrutalBlack
                                )
                            }
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
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Donut Chart
                            NeobrutalExpenseDonutChart(
                                categories = expenseCategories,
                                modifier = Modifier.size(135.dp),
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

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "—",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = textMuted
                                            )
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
        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
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
                        val isInside = sweepAngle >= 22f
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
                        fontSize = if (label.isInside) 10.sp else 9.sp,
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
                    val tickEnd = outerRadius + 3.dp.toPx()
                    drawLine(
                        color = NeobrutalBlack,
                        start = Offset(centerOffset.x + tickStart * cosA, centerOffset.y + tickStart * sinA),
                        end = Offset(centerOffset.x + tickEnd * cosA, centerOffset.y + tickEnd * sinA),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val labelRadius = outerRadius + 8.dp.toPx()
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



