package com.viteats.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.Order
import com.viteats.app.data.remote.OrderListRequest
import com.viteats.app.data.remote.ProodleApi

class OrderRepository(
    private val api: ProodleApi,
    private val sessionManager: SessionManager
) {
    private val gson = Gson()

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
        if (body.startsWith("Error:")) return emptyList()
        
        return try {
            val type = object : TypeToken<List<Order>>() {}.type
            gson.fromJson(body, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOrders(): List<Order> {
        return parseOrders(getRawOrders())
    }

    suspend fun getOrderQR(orderId: String): String? {
        val response = api.getOrderQR(orderId)
        if (!response.isSuccessful) return null
        val body = response.body() ?: return null
        
        if (body.trim() == "0") return null
        return body
    }

    suspend fun checkoutOrder(
        cartItems: List<com.viteats.app.data.model.CartItem>,
        pin: String
    ): Result<String> {
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
            val dtStr = java.text.SimpleDateFormat("dd-MMM-yyyy", java.util.Locale.US).format(cal.time)
            val outletId = cartItems.firstOrNull()?.item?.skuid?.toString() ?: "2"
            val totalAmount = cartItems.sumOf { it.lineTotal }

            val insertItems = cartItems.map { cartItem ->
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
                itemTotal = totalAmount.toInt().toString(),
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
                orderAmount = totalAmount.toInt().toString(),
                ouid = outletId,
                otp = pin,
                p1 = appNumber
            )

            val paymentResponse = api.processPayment(paymentRequest)
            if (!paymentResponse.isSuccessful) {
                return Result.failure(Exception("Payment failed with status code ${paymentResponse.code()}"))
            }

            val paymentBody = paymentResponse.body() ?: ""
            if (paymentBody.startsWith("0") || paymentBody.contains("Invalid", ignoreCase = true)) {
                return Result.failure(Exception("Payment declined: Invalid PIN or insufficient balance."))
            }

            return Result.success(orderNo)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
