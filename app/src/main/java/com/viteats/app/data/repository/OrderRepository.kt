package com.viteats.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.Order
import com.viteats.app.data.remote.OrderListRequest
import com.viteats.app.data.remote.ProodleApi
import com.viteats.app.data.local.OrderDao
import com.viteats.app.data.local.SavedOrder
import com.viteats.app.data.model.CartItem
import com.viteats.app.data.OrderingPolicy
import com.viteats.app.data.AutoOrderSettings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class OrderRepository(
    private val api: ProodleApi,
    private val sessionManager: SessionManager,
    private val dao: OrderDao? = null,
    private val menuRepository: MenuRepository? = null,
    private val settings: AutoOrderSettings? = null
) {
    private val gson = Gson()
    private val checkoutMutex = Mutex()

    fun observeOrders(): Flow<List<Order>> {
        val owner = sessionManager.userIdentifier ?: return flowOf(emptyList())
        return dao?.observe(owner)?.map { rows -> sortOrders(rows.map(::decode)) } ?: flowOf(emptyList())
    }

    private fun decode(row: SavedOrder): Order = gson.fromJson(row.orderJson, Order::class.java)
        .copy(purchasedItems = gson.fromJson(row.itemsJson, object : TypeToken<List<CartItem>>() {}.type))

    private fun sortOrders(orders: List<Order>): List<Order> = orders.sortedByDescending { order ->
        val date = listOf("dd/MMM/yyyy", "dd-MMM-yyyy", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd")
            .firstNotNullOfOrNull { pattern ->
                runCatching { java.text.SimpleDateFormat(pattern, java.util.Locale.US).apply {
                    isLenient = false
                    timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
                }.parse(order.OrderDate)?.time }.getOrNull()
            } ?: 0L
        val time = listOf("hh:mm a", "HH:mm:ss", "HH:mm").firstNotNullOfOrNull { pattern ->
            runCatching {
                val format = java.text.SimpleDateFormat(pattern, java.util.Locale.US).apply {
                    isLenient = false
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                format.parse(order.OrderTime.uppercase(java.util.Locale.US))?.time
            }.getOrNull()
        } ?: 0L
        date + time
    }

    suspend fun savedItems(id: String): List<CartItem> {
        val owner = sessionManager.userIdentifier ?: return emptyList()
        return dao?.find(owner, id)?.let { decode(it).purchasedItems.orEmpty() }.orEmpty()
    }

    suspend fun cachedOrders(): List<Order> {
        val owner = sessionManager.userIdentifier ?: return emptyList()
        return sortOrders(dao?.all(owner)?.map(::decode).orEmpty())
    }

    private suspend fun cache(owner: String, order: Order, items: List<CartItem>? = null) {
        val store = dao ?: return
        store.merge(SavedOrder(owner, order.OrderId, gson.toJson(order),
            items?.let(gson::toJson) ?: "[]"), keepItems = items == null)
    }
    private val _ordersUpdated = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val ordersUpdated: SharedFlow<Unit> = _ordersUpdated.asSharedFlow()

    fun notifyOrdersUpdated() {
        _ordersUpdated.tryEmit(Unit)
    }

    suspend fun getRawOrders(): String {
        val userId = sessionManager.userIdentifier
        if (userId.isNullOrBlank()) {
            sessionManager.notifySessionExpired()
            return "Error: No User Identifier"
        }
        val response = api.getOrderList(OrderListRequest(userId))
        if (response.code() == 401 || response.code() == 403) {
            sessionManager.notifySessionExpired()
            return "Error: Session Expired"
        }
        if (!response.isSuccessful) return "Error: ${response.code()} ${response.message()}"
        return response.body() ?: "Empty body"
    }

    fun parseOrders(body: String): List<Order> {
        if (body.trim() == "0") return emptyList()
        if (body.startsWith("Error:")) throw IllegalStateException("Unable to load order history")
        
        return try {
            val type = object : TypeToken<List<Order>>() {}.type
            gson.fromJson<List<Order>>(body, type) ?: throw IllegalStateException("Empty order history response")
        } catch (e: Exception) {
            throw IllegalStateException("Unable to read order history", e)
        }
    }

    suspend fun getOrders(): List<Order> {
        val owner = sessionManager.userIdentifier ?: return emptyList()
        val orders = parseOrders(getRawOrders())
        if (sessionManager.userIdentifier != owner) return emptyList()
        orders.forEach { cache(owner, it) }
        return if (dao != null) cachedOrders() else orders
    }

    suspend fun getOrderQR(orderId: String): String? {
        val owner = sessionManager.userIdentifier ?: return null
        dao?.find(owner, orderId)?.qrBase64?.let { return it }
        val response = api.getOrderQR(orderId)
        if (!response.isSuccessful) return null
        val body = response.body() ?: return null
        
        if (body.isBlank() || body.trim().removeSurrounding("\"") == "0") return null
        if (sessionManager.userIdentifier != owner) return null
        dao?.saveQr(owner, orderId, body)
        return body
    }

    suspend fun checkoutOrder(
        cartItems: List<com.viteats.app.data.model.CartItem>,
        pin: String,
        budgetOverride: Boolean = false
    ): Result<String> = checkoutMutex.withLock {
        checkoutLocked(cartItems, pin, budgetOverride)
    }

    private suspend fun checkoutLocked(cartItems: List<CartItem>, pin: String, budgetOverride: Boolean): Result<String> {
        val userId = sessionManager.userIdentifier
        val appNumber = sessionManager.registrationNumber
        if (userId.isNullOrBlank() || appNumber.isNullOrBlank()) {
            sessionManager.notifySessionExpired()
            return Result.failure(Exception("Session expired. Please log in again."))
        }
        if (cartItems.isEmpty()) {
            return Result.failure(Exception("Cart is empty."))
        }

        try {
            require(cartItems.all { it.quantity > 0 && it.item.retrt.isFinite() && it.item.retrt >= 0 }) { "Invalid cart items." }
            val currentMenu = menuRepository?.getMenuItems()
            val validatedItems = if (currentMenu != null) OrderingPolicy.reorder(cartItems, currentMenu) else cartItems
            val total = validatedItems.sumOf { it.lineTotal }
            require(kotlin.math.abs(total - cartItems.sumOf { it.lineTotal }) < 0.005) {
                "Menu prices changed. Refresh your cart before confirming payment."
            }
            val limit = settings?.read()?.hardLimit ?: 0.0
            require(budgetOverride || !OrderingPolicy.exceedsLimit(total, limit)) { "Order exceeds your hard budget limit. Review your cart and confirm again." }
            require(validatedItems.map { it.item.skuid }.distinct().size == 1) { "Please order from one outlet at a time." }
            require(sessionManager.userIdentifier == userId) { "Session changed. Please try again." }
            // Step 1: Generate Order ID
            val orderIdResponse = api.getOrderId(com.viteats.app.data.remote.OrderIdRequest(mobileNo = userId))
            if (!orderIdResponse.isSuccessful || orderIdResponse.body().isNullOrEmpty()) {
                if (orderIdResponse.code() == 401 || orderIdResponse.code() == 403) {
                    sessionManager.notifySessionExpired()
                }
                return Result.failure(Exception("Failed to generate Order ID from server (${orderIdResponse.code()})"))
            }

            val orderNo = orderIdResponse.body()!!.first().orderNo
            if (orderNo.isBlank()) {
                return Result.failure(Exception("Invalid Order ID received from server."))
            }

            // Step 2: Build and Insert Order
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
            val dtStr = java.text.SimpleDateFormat("dd-MMM-yyyy", java.util.Locale.US).apply {
                timeZone = cal.timeZone
            }.format(cal.time)
            val outletId = validatedItems.first().item.skuid.toString()
            val totalAmount = total
            val amountText = java.math.BigDecimal.valueOf(totalAmount)
                .setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()

            val insertItems = validatedItems.map { cartItem ->
                val item = cartItem.item
                com.viteats.app.data.remote.OrderInsertItem(
                    productId = "${item.skuid}_${item.meitid}_2",
                    quantity = cartItem.quantity,
                    ides = item.meitdes,
                    rt = item.retrt,
                    amt = cartItem.lineTotal,
                    tp = "P",
                    odt = dtStr,
                    odtdes = item.odtdes,
                    tb = 2,
                    pid = item.meitid.toString(),
                    dtstr = dtStr,
                    ldes = item.dispname,
                    cal = "",
                    sname = item.odtdes,
                    skid = item.skuid.toString(),
                    flag = 1,
                    optcls = ""
                )
            }

            val insertRequest = com.viteats.app.data.remote.OrderInsertRequest(
                items = insertItems,
                orderNumber = orderNo,
                tableNo = "1",
                itemTotal = amountText,
                outLetId = outletId,
                mobileNo = userId,
                refNo = ""
            )

            val insertResponse = api.insertOrder(insertRequest)
            if (!insertResponse.isSuccessful) {
                return Result.failure(Exception("Failed to submit order items (${insertResponse.code()})"))
            }

            // Step 3: Process Payment
            val paymentRequest = com.viteats.app.data.remote.PaymentRequest(
                mobileNo = userId,
                orderNumber = orderNo,
                orderAmount = amountText,
                ouid = outletId,
                otp = pin,
                p1 = appNumber
            )

            val localOrder = Order(dtStr, java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).apply {
                timeZone = cal.timeZone
            }.format(cal.time), totalAmount, orderNo, "Pending", "", validatedItems.joinToString(", ") { it.item.meitdes },
                0, appNumber, "", validatedItems)
            // Keep the exact basket even if the payment response is interrupted.
            cache(userId, localOrder, validatedItems)

            val paymentResponse = api.processPayment(paymentRequest)
            if (!paymentResponse.isSuccessful) {
                return Result.failure(Exception("Payment failed with status code ${paymentResponse.code()}"))
            }

            val paymentBody = paymentResponse.body()?.trim()?.removeSurrounding("\"") ?: ""
            val paymentParts = paymentBody.split('|', limit = 2)
            if (paymentParts.size != 2 || paymentParts[0] != "1" || paymentParts[1].isBlank()) {
                return Result.failure(Exception("Payment was not confirmed. Check your orders and wallet before trying again."))
            }

            // A confirmed payment must not be retried because saving a QR failed.
            try {
                cache(userId, localOrder.copy(Status = "Success"), validatedItems)
                if (sessionManager.userIdentifier == userId) getOrderQR(orderNo)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { /* History/QR refresh can recover after reconnecting. */ }
            notifyOrdersUpdated()
            return Result.success(orderNo)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
