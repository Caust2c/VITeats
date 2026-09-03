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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.components.NeobrutalPill
import com.viteats.app.ui.theme.*
import com.viteats.app.util.MealPeriodHelper
import com.viteats.app.util.MealType
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

                // --- Budget Tracking Card ---
                val remainingBudget = balance.bal
                val totalBudget = remember(balance.bal) {
                    if (balance.bal > 0) {
                        val base = ((balance.bal / 1000).toInt() + 2) * 1000.0
                        maxOf(base, 5000.0)
                    } else 5000.0
                }
                val spentBudget = (totalBudget - remainingBudget).coerceAtLeast(0.0)
                val budgetProgress = if (totalBudget > 0) {
                    ((totalBudget - remainingBudget) / totalBudget).toFloat().coerceIn(0.06f, 1.0f)
                } else 0.5f

                val calendar = remember { Calendar.getInstance() }
                val daysInMonth = remember(calendar) { calendar.getActualMaximum(Calendar.DAY_OF_MONTH) }
                val currentDay = remember(calendar) { calendar.get(Calendar.DAY_OF_MONTH) }
                val remainingDays = remember(daysInMonth, currentDay) { (daysInMonth - currentDay + 1).coerceAtLeast(1) }
                val perDayAmount = remember(remainingBudget, remainingDays) {
                    (remainingBudget / remainingDays).toInt().coerceAtLeast(0)
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
                                text = "This Month",
                                backgroundColor = PastelYellow,
                                textColor = NeobrutalBlack,
                                isSelected = false
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
                                text = "₹${spentBudget.toInt()} / ₹${totalBudget.toInt()}",
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
                                    text = "You can spend ₹$perDayAmount per day for the rest of the month",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = NeobrutalBlack
                                )
                            }
                        }
                    }
                }

                // --- Expense Breakdown Card ---
                val expenseCategories = remember(balance.bal) {
                    val baseSpent = if (balance.bal > 0) maxOf(balance.bal * 1.5, 3000.0) else 3750.0
                    listOf(
                        ExpenseCategory("Lunch", baseSpent * 0.42, MintGreen),
                        ExpenseCategory("Dinner", baseSpent * 0.30, SoftCyan),
                        ExpenseCategory("Snacks", baseSpent * 0.18, PastelYellow),
                        ExpenseCategory("Breakfast", baseSpent * 0.10, SoftCoral)
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

                            NeobrutalPill(
                                text = "Meal Stats",
                                backgroundColor = SoftCyan,
                                textColor = NeobrutalBlack,
                                isSelected = false
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
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
                                        text = "₹${expenseCategories.sumOf { it.amount }.toInt()}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                }
                            }

                            // Bold Legend
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val totalExp = expenseCategories.sumOf { it.amount }
                                expenseCategories.forEach { cat ->
                                    val pct = if (totalExp > 0) ((cat.amount / totalExp) * 100).toInt() else 0
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
                                                    .size(14.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(cat.color)
                                                    .border(1.5.dp, NeobrutalBlack, RoundedCornerShape(4.dp))
                                            )
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary
                                            )
                                        }

                                        Text(
                                            text = "$pct%",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Black,
                                            color = textPrimary
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

data class ExpenseCategory(
    val name: String,
    val amount: Double,
    val color: Color
)

@Composable
fun NeobrutalExpenseDonutChart(
    categories: List<ExpenseCategory>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 20.dp,
    centerContent: (@Composable () -> Unit)? = null
) {
    val total = remember(categories) { categories.sumOf { it.amount } }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val strokeWidthPx = strokeWidth.toPx()
            val borderStrokePx = 2.dp.toPx()
            val arcSize = size.minDimension - strokeWidthPx
            val topLeftOffset = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            var startAngle = -90f

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
                    startAngle += sweepAngle
                }
            }

            // Neobrutalist outer circle border
            val outerRadius = size.minDimension / 2
            drawCircle(
                color = NeobrutalBlack,
                radius = outerRadius,
                style = Stroke(width = borderStrokePx)
            )

            // Neobrutalist inner circle border
            val innerRadius = outerRadius - strokeWidthPx
            if (innerRadius > 0f) {
                drawCircle(
                    color = NeobrutalBlack,
                    radius = innerRadius,
                    style = Stroke(width = borderStrokePx)
                )
            }
        }

        centerContent?.invoke()
    }
}



