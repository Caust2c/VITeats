package com.viteats.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.BudgetManager
import com.viteats.app.data.indiaCalendar
import com.viteats.app.data.remote.BalanceResponse
import com.viteats.app.data.remote.Order
import com.viteats.app.data.repository.OrderRepository
import com.viteats.app.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Calendar
import java.util.Locale

sealed class BalanceState {
    object Loading : BalanceState()
    data class Success(val balance: BalanceResponse) : BalanceState()
    data class Error(val message: String) : BalanceState()
}

data class CategorySpending(
    val lunch: Double = 0.0,
    val dinner: Double = 0.0,
    val snacks: Double = 0.0,
    val breakfast: Double = 0.0,
    val totalSpent: Double = 0.0
) {
    val lunchPercentage: Int
        get() = if (totalSpent > 0) Math.round((lunch / totalSpent) * 100).toInt() else 0

    val dinnerPercentage: Int
        get() = if (totalSpent > 0) Math.round((dinner / totalSpent) * 100).toInt() else 0

    val snacksPercentage: Int
        get() = if (totalSpent > 0) Math.round((snacks / totalSpent) * 100).toInt() else 0

    val breakfastPercentage: Int
        get() = if (totalSpent > 0) Math.round((breakfast / totalSpent) * 100).toInt() else 0
}

class StudentViewModel(
    private val repository: StudentRepository,
    private val budgetManager: BudgetManager? = null,
    private val orderRepository: OrderRepository? = null,
    private val calendarProvider: () -> Calendar = ::indiaCalendar
) : ViewModel() {
    private val _balanceState = MutableStateFlow<BalanceState>(BalanceState.Loading)
    val balanceState: StateFlow<BalanceState> = _balanceState

    private val _initialMonthlyAllocation = MutableStateFlow<Double>(
        budgetManager?.getInitialMonthlyAllocation() ?: 0.0
    )
    val initialMonthlyAllocation: StateFlow<Double> = _initialMonthlyAllocation.asStateFlow()

    // Backwards-compatible alias for existing tests
    val budgetLimit: StateFlow<Double> = _initialMonthlyAllocation

    private val _currentSpending = MutableStateFlow<Double>(0.0)
    val currentSpending: StateFlow<Double> = _currentSpending.asStateFlow()

    private val _isMonthRolledOver = MutableStateFlow<Boolean>(false)
    val isMonthRolledOver: StateFlow<Boolean> = _isMonthRolledOver.asStateFlow()

    private val _categorySpending = MutableStateFlow(CategorySpending())
    val categorySpending: StateFlow<CategorySpending> = _categorySpending.asStateFlow()

    private val _completedOrders = MutableStateFlow<List<Order>>(emptyList())
    val completedOrders: StateFlow<List<Order>> = _completedOrders.asStateFlow()

    private val _expensesLoading = MutableStateFlow(true)
    val expensesLoading: StateFlow<Boolean> = _expensesLoading.asStateFlow()
    private val _expensesError = MutableStateFlow<String?>(null)
    val expensesError: StateFlow<String?> = _expensesError.asStateFlow()

    init {
        checkMonthlyReset()
    }

    fun checkMonthlyReset() {
        val resetNow = budgetManager?.checkAndResetMonthlyRollover() ?: false
        val didReset = resetNow || budgetManager?.wasRolledOver == true
        if (didReset) {
            _initialMonthlyAllocation.value = 0.0
            _isMonthRolledOver.value = true
        } else {
            _initialMonthlyAllocation.value = budgetManager?.getInitialMonthlyAllocation() ?: _initialMonthlyAllocation.value
        }
        processOrdersForExpenses(_completedOrders.value)
    }

    fun setInitialMonthlyAllocation(allocation: Double) {
        if (allocation.isFinite() && allocation >= 0) {
            _initialMonthlyAllocation.value = allocation
            budgetManager?.setInitialMonthlyAllocation(allocation)
            _isMonthRolledOver.value = false
            recalculateSpending()
        }
    }

    fun updateBudgetLimit(newLimit: Double) {
        setInitialMonthlyAllocation(newLimit)
    }

    fun updateCurrentSpending(spending: Double) {
        if (spending.isFinite() && spending >= 0) {
            _currentSpending.value = spending
        }
    }

    fun recalculateSpending() {
        _currentSpending.value = _categorySpending.value.totalSpent
    }

    fun fetchBalance() {
        viewModelScope.launch {
            _balanceState.value = BalanceState.Loading
            try {
                val response = repository.getBalance()
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val balance = response.body()!![0]
                    _balanceState.value = BalanceState.Success(balance)
                    recalculateSpending()
                } else {
                    _balanceState.value = BalanceState.Error("Failed to fetch balance")
                }
            } catch (e: Exception) {
                _balanceState.value = BalanceState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private var isObservingOrders = false
    private var expensesJob: kotlinx.coroutines.Job? = null

    fun fetchOrdersAndExpenses() {
        checkMonthlyReset()
        val repo = orderRepository ?: return
        if (!isObservingOrders) {
            isObservingOrders = true
            viewModelScope.launch {
                repo.ordersUpdated.collect {
                    fetchOrdersAndExpenses()
                    fetchBalance()
                }
            }
        }
        expensesJob?.cancel()
        expensesJob = viewModelScope.launch {
            _expensesLoading.value = true
            _expensesError.value = null
            try {
                val orders = repo.getOrders()
                processOrdersForExpenses(orders)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                _expensesError.value = "Couldn't refresh monthly expenses. Retry to update your budget."
            } finally {
                _expensesLoading.value = false
            }
        }
    }

    fun processOrdersForExpenses(orders: List<Order>) {
        val validOrders = orders.filter { order ->
            (order.Status.trim().equals("Success", ignoreCase = true) ||
                order.Status.trim().equals("Delivered", ignoreCase = true)) &&
                order.NetAmount.isFinite() && order.NetAmount >= 0
        }.distinctBy { it.OrderId }
        _completedOrders.value = validOrders

        val currentMonthOrders = validOrders.filter { isOrderInCurrentMonth(it.OrderDate) }

        var lunchSum = 0.0
        var dinnerSum = 0.0
        var snacksSum = 0.0
        var breakfastSum = 0.0

        for (order in currentMonthOrders) {
            val cat = classifyOrderCategory(order)
            val amt = if (order.NetAmount > 0) order.NetAmount else 0.0
            when (cat) {
                "Lunch" -> lunchSum += amt
                "Dinner" -> dinnerSum += amt
                "Snacks" -> snacksSum += amt
                "Breakfast" -> breakfastSum += amt
            }
        }

        val total = lunchSum + dinnerSum + snacksSum + breakfastSum
        _categorySpending.value = CategorySpending(
            lunch = lunchSum,
            dinner = dinnerSum,
            snacks = snacksSum,
            breakfast = breakfastSum,
            totalSpent = total
        )

        recalculateSpending()
    }

    fun classifyOrderCategory(order: Order): String {
        val snameLower = order.sname.lowercase(Locale.ROOT)

        // 1. Direct sname matches
        if (snameLower.contains("breakfast")) return "Breakfast"
        if (snameLower.contains("lunch")) return "Lunch"
        if (snameLower.contains("dinner")) return "Dinner"
        if (snameLower.contains("snack")) return "Snacks"

        // 2. Keyword matching on items in sname
        val breakfastKeywords = listOf("dosa", "idli", "vada", "poori", "puri", "upma", "pongal", "omelette", "egg", "paratha", "poha", "morning")
        val lunchKeywords = listOf("biryani", "thali", "curd rice", "sambar", "meals", "chapati", "roti", "paneer", "dal", "afternoon", "rice")
        val snacksKeywords = listOf("samosa", "puff", "sandwich", "maggi", "noodles", "juice", "shake", "biscuit", "roll", "chaat", "tea", "coffee", "beverage")
        val dinnerKeywords = listOf("fried rice", "naan", "tandoori", "parotta", "night")

        if (breakfastKeywords.any { snameLower.contains(it) }) return "Breakfast"
        if (dinnerKeywords.any { snameLower.contains(it) }) return "Dinner"
        if (snacksKeywords.any { snameLower.contains(it) }) return "Snacks"
        if (lunchKeywords.any { snameLower.contains(it) }) return "Lunch"

        // 3. Fallback to OrderTime
        val timeCategory = classifyByOrderTime(order.OrderTime)
        if (timeCategory != null) return timeCategory

        return "Lunch"
    }

    fun classifyByOrderTime(timeStr: String): String? {
        if (timeStr.isBlank()) return null
        val clean = timeStr.trim().lowercase(Locale.ROOT)

        val isPm = clean.contains("pm")
        val isAm = clean.contains("am")

        val digitsOnly = clean.replace(Regex("[^0-9:]"), "")
        val parts = digitsOnly.split(":")
        if (parts.isEmpty()) return null

        var hour = parts[0].toIntOrNull() ?: return null
        val minute = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0

        if (minute !in 0..59 || hour !in 0..23 || ((isAm || isPm) && hour !in 1..12)) return null

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        val minutes = hour * 60 + minute

        return when (minutes) {
            in 360..689 -> "Breakfast"   // 06:00 - 11:29
            in 690..959 -> "Lunch"       // 11:30 - 15:59
            in 960..1124 -> "Snacks"     // 16:00 - 18:44
            else -> "Dinner"             // 18:45 - 23:59, 00:00 - 05:59
        }
    }

    fun isOrderInCurrentMonth(orderDateStr: String): Boolean {
        if (orderDateStr.isBlank()) return false
        val clean = orderDateStr.trim()
        val now = calendarProvider()
        val formats = listOf(
            "dd/MMM/yyyy", "dd-MMM-yyyy", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US).apply {
                    isLenient = false
                    timeZone = now.timeZone
                }
                val position = ParsePosition(0)
                val date = sdf.parse(clean, position)
                if (date != null && position.index == clean.length) {
                    val cal = calendarProvider()
                    cal.time = date
                    val orderYear = cal.get(Calendar.YEAR)
                    val orderMonth = cal.get(Calendar.MONTH) + 1
                    if (orderYear == now.get(Calendar.YEAR) && orderMonth == now.get(Calendar.MONTH) + 1 && !date.after(now.time)) {
                        return true
                    }
                }
            } catch (_: Exception) {}
        }

        return false
    }
}
